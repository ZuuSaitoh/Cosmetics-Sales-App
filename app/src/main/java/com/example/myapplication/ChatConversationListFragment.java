package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.Conversation;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ConversationService;
import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.ChatMessageService;
import com.example.myapplication.network.dto.ChatMessageDto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatConversationListFragment extends Fragment {

    private RecyclerView rvConversations;
    private ProgressBar progress;
    private TextView tvEmpty;
    private ConversationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_conversation_list, container, false);
        rvConversations = root.findViewById(R.id.rvConversations);
        progress = root.findViewById(R.id.progress);
        tvEmpty = root.findViewById(R.id.tvEmpty);

        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConversations.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        adapter = new ConversationAdapter();
        rvConversations.setAdapter(adapter);

        // Swipe to delete (auto delete without confirm)
        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                if (pos >= 0) adapter.removeAt(pos);
            }
        }).attachToRecyclerView(rvConversations);

        loadConversations();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // refresh to update highlight after returning from chat
        loadConversations();
    }

    private void loadConversations() {
        progress.setVisibility(View.VISIBLE);
        ConversationService service = ApiClient.getRetrofit(requireContext()).create(ConversationService.class);
        service.getAllConversations().enqueue(new Callback<ApiResponse<List<Conversation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Conversation>>> call, Response<ApiResponse<List<Conversation>>> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    List<Conversation> list = response.body().getResult();
                    adapter.setItems(list);
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Conversation>>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private class ConversationAdapter extends RecyclerView.Adapter<ConversationViewHolder> {
        private final List<Conversation> items = new ArrayList<>();
        private final java.util.Map<Long, ChatMessageDto> latestMessageMap = new java.util.HashMap<>();
        private final java.util.Set<Long> fetchingSet = new java.util.HashSet<>();

        void setItems(List<Conversation> data) {
            items.clear();
            latestMessageMap.clear();
            fetchingSet.clear();
            if (data != null) items.addAll(data);
            // Fetch all latest messages in parallel, then sort
            fetchAllLatest();
        }

        @NonNull
        @Override
        public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
            return new ConversationViewHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
            Conversation c = items.get(position);
            Long cid = c.getConversationID();
            String title = c.getUser() != null ? c.getUser().getUsername() : ("Conversation #" + cid);
            holder.title.setText(title);

            // Check if we already have the latest message
            ChatMessageDto latest = latestMessageMap.get(cid);
            if (latest != null) {
                String snippet = latest.getMessage() != null ? latest.getMessage() : "";
                holder.snippet.setText(snippet.isEmpty() ? "Chưa có tin nhắn" : snippet);
                holder.time.setText(formatShortTime(latest.getSentAt()));
                holder.unreadDot.setVisibility(isUnseen(cid, latest) ? View.VISIBLE : View.GONE);
                applyHighlight(holder.itemView, cid, latest);
            } else {
                holder.snippet.setText("Đang tải tin nhắn...");
                holder.time.setText("");
                holder.unreadDot.setVisibility(View.GONE);
                applyHighlight(holder.itemView, cid, null);
            }

            holder.itemView.setOnClickListener(v -> {
                // Open ChatActivity with conversationId
                android.content.Intent i = new android.content.Intent(requireContext(), ChatActivity.class);
                i.putExtra("conversationId", cid);
                i.putExtra("isAdmin", true);
                startActivity(i);

                // mark latest as seen
                ChatMessageDto latestMsg = latestMessageMap.get(cid);
                if (latestMsg != null && latestMsg.getChatMessageID() != null) {
                    saveSeenLatest(cid, latestMsg.getChatMessageID());
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private void fetchAllLatest() {
            for (Conversation c : items) {
                Long cid = c.getConversationID();
                if (cid == null || fetchingSet.contains(cid)) continue;
                fetchingSet.add(cid);
                fetchLatestForConversation(cid, last -> {
                    latestMessageMap.put(cid, last);
                    fetchingSet.remove(cid);
                    // Once all fetched, sort and update
                    if (fetchingSet.isEmpty()) {
                        sortByLatest();
                        notifyDataSetChanged();
                    }
                });
            }
            // If no items, just notify
            if (items.isEmpty()) {
                notifyDataSetChanged();
            }
        }

        void removeAt(int pos) {
            if (pos < 0 || pos >= items.size()) return;
            Conversation removed = items.remove(pos);
            latestMessageMap.remove(removed.getConversationID());
            notifyItemRemoved(pos);
        }

        private void sortByLatest() {
            java.util.Collections.sort(items, (a, b) -> {
                Long aId = a.getConversationID();
                Long bId = b.getConversationID();
                
                ChatMessageDto aLatest = latestMessageMap.get(aId);
                ChatMessageDto bLatest = latestMessageMap.get(bId);
                
                // Ưu tiên conversation có tin nhắn mới chưa đọc lên đầu
                boolean aUnseen = isConversationUnseen(aId, aLatest);
                boolean bUnseen = isConversationUnseen(bId, bLatest);
                
                if (aUnseen && !bUnseen) return -1; // a có tin mới chưa đọc, đẩy lên
                if (!aUnseen && bUnseen) return 1;  // b có tin mới chưa đọc, đẩy b lên
                
                // Cùng trạng thái (đều có hoặc không có tin mới), sort theo thời gian latest message
                // Nếu cả hai đều đã seen, vẫn sort theo thời gian để conversation mới nhất ở đầu
                long ta = getTimestampFor(a);
                long tb = getTimestampFor(b);
                
                // Nếu có latest message, ưu tiên theo thời gian latest message
                if (aLatest != null && bLatest != null) {
                    return Long.compare(parseIsoMillis(bLatest.getSentAt()), parseIsoMillis(aLatest.getSentAt()));
                }
                if (aLatest != null) return -1; // a có latest message, đẩy lên
                if (bLatest != null) return 1;  // b có latest message, đẩy b lên
                
                // Cả hai đều không có latest message, sort theo createdAt
                return Long.compare(parseIsoMillis(b.getCreatedAt()), parseIsoMillis(a.getCreatedAt()));
            });
        }
        
        private boolean isConversationUnseen(Long conversationId, ChatMessageDto latest) {
            if (conversationId == null || latest == null || latest.getChatMessageID() == null) return false;
            android.content.SharedPreferences sp = requireContext().getSharedPreferences("chat_admin_seen", android.content.Context.MODE_PRIVATE);
            long seenId = sp.getLong("seen_" + conversationId, -1L);
            return latest.getChatMessageID() > seenId;
        }

        private long getTimestampFor(Conversation c) {
            ChatMessageDto last = latestMessageMap.get(c.getConversationID());
            String iso = last != null ? last.getSentAt() : c.getCreatedAt();
            return parseIsoMillis(iso);
        }
        
        private long parseIsoMillis(String iso) {
            if (iso == null) return 0L;
            try {
                java.util.Date d = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()).parse(iso);
                return d != null ? d.getTime() : 0L;
            } catch (Exception e) {
                return 0L;
            }
        }
    }

    private static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView title, snippet, time;
        View unreadDot;
        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvName);
            snippet = itemView.findViewById(R.id.tvSnippet);
            time = itemView.findViewById(R.id.tvTime);
            unreadDot = itemView.findViewById(R.id.unreadDot);
        }
    }

    private String formatDate(String iso) {
        if (iso == null) return "";
        try {
            java.util.Date d = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()).parse(iso);
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(d);
        } catch (Exception e) {
            return iso;
        }
    }

    private interface LatestCallback { void onLatest(ChatMessageDto last); }

    private void fetchLatestForConversation(Long conversationId, LatestCallback cb) {
        if (conversationId == null) { 
            cb.onLatest(null); 
            return; 
        }
        ChatMessageService svc = ApiClient.getRetrofit(requireContext()).create(ChatMessageService.class);
        svc.getMessagesByConversation(conversationId).enqueue(new Callback<ApiResponse<List<ChatMessageDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChatMessageDto>>> call, Response<ApiResponse<List<ChatMessageDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    List<ChatMessageDto> list = response.body().getResult();
                    ChatMessageDto last = list.isEmpty() ? null : list.get(list.size() - 1);
                    cb.onLatest(last);
                } else {
                    cb.onLatest(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChatMessageDto>>> call, Throwable t) {
                cb.onLatest(null);
            }
        });
    }

    private void applyHighlight(View itemView, Long conversationId, ChatMessageDto latest) {
        if (conversationId == null) return;
        long seenId = getSeenLatest(conversationId);
        long latestId = latest != null && latest.getChatMessageID() != null ? latest.getChatMessageID() : -1L;
        boolean unseen = latestId != -1L && latestId > seenId;
        itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
    }

    private boolean isUnseen(Long conversationId, ChatMessageDto latest) {
        if (conversationId == null || latest == null || latest.getChatMessageID() == null) return false;
        long seenId = getSeenLatest(conversationId);
        return latest.getChatMessageID() > seenId;
    }

    private long getSeenLatest(long conversationId) {
        android.content.SharedPreferences sp = requireContext().getSharedPreferences("chat_admin_seen", android.content.Context.MODE_PRIVATE);
        return sp.getLong("seen_" + conversationId, -1L);
    }

    private void saveSeenLatest(long conversationId, long messageId) {
        android.content.SharedPreferences sp = requireContext().getSharedPreferences("chat_admin_seen", android.content.Context.MODE_PRIVATE);
        sp.edit().putLong("seen_" + conversationId, messageId).apply();
    }

    private long parseIsoMillis(String iso) {
        if (iso == null) return 0L;
        try {
            java.util.Date d = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()).parse(iso);
            return d != null ? d.getTime() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private String formatShortTime(String iso) {
        try {
            long ms = parseIsoMillis(iso);
            java.text.DateFormat df = new java.text.SimpleDateFormat("HH:mm", Locale.getDefault());
            return ms > 0 ? df.format(new java.util.Date(ms)) : "";
        } catch (Exception e) {
            return "";
        }
    }
}


