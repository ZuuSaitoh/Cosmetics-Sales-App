package com.example.myapplication.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class CartAnimation {
    
    public interface AnimationListener {
        void onAnimationStart();
        void onAnimationEnd();
    }
    
    /**
     * Tạo animation bay từ vị trí hiện tại đến cart icon với hình ảnh sản phẩm
     * @param sourceView View nguồn (thường là button add to cart)
     * @param targetView View đích (cart icon)
     * @param productImage ImageView của sản phẩm để làm animation
     * @param listener Callback khi animation hoàn thành
     */
    public static void flyToCart(View sourceView, View targetView, android.widget.ImageView productImage, AnimationListener listener) {
        if (sourceView == null || targetView == null) {
            if (listener != null) listener.onAnimationEnd();
            return;
        }
        
        // Lấy vị trí của source và target
        int[] sourceLocation = new int[2];
        int[] targetLocation = new int[2];
        
        sourceView.getLocationOnScreen(sourceLocation);
        targetView.getLocationOnScreen(targetLocation);
        
        // Tính toán offset để animation bay từ center của source đến center của target
        float startX = sourceLocation[0] + sourceView.getWidth() / 2f;
        float startY = sourceLocation[1] + sourceView.getHeight() / 2f;
        float endX = targetLocation[0] + targetView.getWidth() / 2f;
        float endY = targetLocation[1] + targetView.getHeight() / 2f;
        
        // Tạo một view tạm thời để animation
        View animationView = createAnimationView(sourceView, productImage);
        if (animationView == null) {
            if (listener != null) listener.onAnimationEnd();
            return;
        }
        
        // Thêm view vào parent của sourceView
        android.view.ViewGroup parent = (android.view.ViewGroup) sourceView.getParent();
        if (parent != null) {
            parent.addView(animationView);
            
            // Set vị trí ban đầu
            animationView.setX(startX - animationView.getWidth() / 2f);
            animationView.setY(startY - animationView.getHeight() / 2f);
            
            // Tạo animation path với đường cong
            Path path = createCurvedPath(startX, startY, endX, endY);
            
            // Tạo ObjectAnimator cho translation
            ObjectAnimator pathAnimator = ObjectAnimator.ofFloat(animationView, "x", "y", path);
            pathAnimator.setDuration(800);
            pathAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            
            // Tạo animation cho scale (phóng to rồi thu nhỏ)
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(animationView, "scaleX", 1.0f, 1.3f, 0.8f);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(animationView, "scaleY", 1.0f, 1.3f, 0.8f);
            
            scaleXAnimator.setDuration(800);
            scaleYAnimator.setDuration(800);
            scaleXAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleYAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            
            // Tạo animation cho alpha (mờ dần)
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(animationView, "alpha", 1.0f, 0.0f);
            alphaAnimator.setDuration(600);
            alphaAnimator.setStartDelay(200);
            alphaAnimator.setInterpolator(new DecelerateInterpolator());
            
            // Tạo animation cho rotation (xoay nhẹ)
            ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(animationView, "rotation", 0f, 360f);
            rotationAnimator.setDuration(800);
            rotationAnimator.setInterpolator(new AccelerateInterpolator());
            
            // Kết hợp tất cả animation
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(pathAnimator, scaleXAnimator, scaleYAnimator, alphaAnimator, rotationAnimator);
            
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (listener != null) listener.onAnimationStart();
                }
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Xóa view tạm thời
                    if (parent != null) {
                        parent.removeView(animationView);
                    }
                    if (listener != null) listener.onAnimationEnd();
                }
            });
            
            animatorSet.start();
        }
    }
    
    /**
     * Tạo animation bay từ vị trí hiện tại đến cart icon (backward compatibility)
     * @param sourceView View nguồn (thường là button add to cart)
     * @param targetView View đích (cart icon)
     * @param listener Callback khi animation hoàn thành
     */
    public static void flyToCart(View sourceView, View targetView, AnimationListener listener) {
        flyToCart(sourceView, targetView, null, listener);
    }
    
    /**
     * Tạo view tạm thời để animation với hình ảnh sản phẩm
     */
    private static View createAnimationView(View sourceView, android.widget.ImageView productImage) {
        // Tạo một ImageView để hiển thị hình ảnh sản phẩm
        android.widget.ImageView animationView = new android.widget.ImageView(sourceView.getContext());
        
        // Set kích thước
        int size = Math.min(sourceView.getWidth(), sourceView.getHeight());
        if (size <= 0) size = 64; // Default size
        
        animationView.setLayoutParams(new android.view.ViewGroup.LayoutParams(size, size));
        
        // Copy hình ảnh từ productImage nếu có
        if (productImage != null && productImage.getDrawable() != null) {
            animationView.setImageDrawable(productImage.getDrawable());
            animationView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        } else {
            // Fallback: sử dụng icon mặc định
            animationView.setImageResource(android.R.drawable.ic_menu_add);
            animationView.setBackgroundColor(0xFF4CAF50); // Màu xanh lá
        }
        
        // Thêm border tròn (sử dụng drawable có sẵn)
        animationView.setBackgroundResource(android.R.drawable.ic_menu_add);
        
        return animationView;
    }
    
    /**
     * Tạo đường cong cho animation path
     */
    private static Path createCurvedPath(float startX, float startY, float endX, float endY) {
        Path path = new Path();
        path.moveTo(startX, startY);
        
        // Tính toán điểm control để tạo đường cong
        float controlX = (startX + endX) / 2f;
        float controlY = Math.min(startY, endY) - 100f; // Bay cao hơn
        
        path.quadTo(controlX, controlY, endX, endY);
        
        return path;
    }
    
    /**
     * Tạo animation bay với hình ảnh sản phẩm từ Product object
     * @param sourceView View nguồn
     * @param targetView View đích (cart icon)
     * @param productImage ImageView của sản phẩm
     * @param listener Callback
     */
    public static void flyToCartWithProduct(View sourceView, View targetView, android.widget.ImageView productImage, AnimationListener listener) {
        flyToCart(sourceView, targetView, productImage, listener);
    }
    
    /**
     * Tạo animation bay với hình ảnh sản phẩm từ drawable resource
     * @param sourceView View nguồn
     * @param targetView View đích (cart icon)
     * @param context Context để tạo ImageView
     * @param drawableResId Resource ID của hình ảnh sản phẩm
     * @param listener Callback
     */
    public static void flyToCartWithDrawable(View sourceView, View targetView, android.content.Context context, int drawableResId, AnimationListener listener) {
        android.widget.ImageView productImage = new android.widget.ImageView(context);
        productImage.setImageResource(drawableResId);
        flyToCart(sourceView, targetView, productImage, listener);
    }
    
    /**
     * Tạo animation bay với hình ảnh sản phẩm từ URL (sử dụng Glide)
     * @param sourceView View nguồn
     * @param targetView View đích (cart icon)
     * @param context Context để tạo ImageView
     * @param imageUrl URL của hình ảnh sản phẩm
     * @param listener Callback
     */
    public static void flyToCartWithUrl(View sourceView, View targetView, android.content.Context context, String imageUrl, AnimationListener listener) {
        android.widget.ImageView productImage = new android.widget.ImageView(context);
        
        // Sử dụng Glide để load hình ảnh từ URL
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                com.bumptech.glide.Glide.with(context)
                    .load(imageUrl.trim())
                    .placeholder(android.R.drawable.ic_menu_add)
                    .error(android.R.drawable.ic_menu_add)
                    .into(productImage);
            } catch (Exception e) {
                // Fallback nếu có lỗi
                productImage.setImageResource(android.R.drawable.ic_menu_add);
            }
        } else {
            productImage.setImageResource(android.R.drawable.ic_menu_add);
        }
        
        flyToCart(sourceView, targetView, productImage, listener);
    }
    
    /**
     * Tạo animation bay với hình ảnh sản phẩm từ Product object
     * @param sourceView View nguồn
     * @param targetView View đích (cart icon)
     * @param context Context để tạo ImageView
     * @param product Product object chứa thông tin sản phẩm
     * @param listener Callback
     */
    public static void flyToCartWithProduct(View sourceView, View targetView, android.content.Context context, com.example.myapplication.model.Product product, AnimationListener listener) {
        if (product != null && product.getImageURL() != null && !product.getImageURL().isEmpty()) {
            // Sử dụng URL hình ảnh sản phẩm
            flyToCartWithUrl(sourceView, targetView, context, product.getImageURL(), listener);
        } else {
            // Fallback: sử dụng hình ảnh mặc định
            flyToCartWithDrawable(sourceView, targetView, context, android.R.drawable.ic_menu_add, listener);
        }
    }
    
    /**
     * Animation đơn giản hơn - chỉ scale và fade
     */
    public static void simpleFlyToCart(View sourceView, View targetView, AnimationListener listener) {
        if (sourceView == null || targetView == null) {
            if (listener != null) listener.onAnimationEnd();
            return;
        }
        
        // Tạo animation đơn giản cho sourceView
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sourceView, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sourceView, "scaleY", 1.0f, 1.2f, 1.0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null) listener.onAnimationStart();
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) listener.onAnimationEnd();
            }
        });
        
        animatorSet.start();
    }
}
