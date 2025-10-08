package com.example.demo.service;

import com.example.demo.dto.request.StoreLocationCreateRequest;
import com.example.demo.entity.StoreLocation;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.StoreLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StoreLocationService {
    @Autowired
    StoreLocationRepository repository;

    public StoreLocation create(StoreLocationCreateRequest request){
        StoreLocation location = StoreLocation.builder()
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .build();
        return repository.save(location);
    }

    public List<StoreLocation> getAll(){
        return repository.findAll();
    }

    public StoreLocation getById(Integer id){
        return repository.findById(id).orElseThrow(()->new AppException(ErrorCode.LOCATION_NOT_EXISTED));
    }

    public StoreLocation update(Integer id, StoreLocationCreateRequest request){
        StoreLocation storeLocation = getById(id);
        if (storeLocation.getLatitude()!=null)
            storeLocation.setLatitude(request.getLatitude());
        if (storeLocation.getLongitude()!=null)
            storeLocation.setLongitude(request.getLongitude());
        if (storeLocation.getAddress()!=null)
            storeLocation.setAddress(request.getAddress());
        return repository.save(storeLocation);
    }

    public void deleteStoreLocation(Integer id){
        repository.deleteById(id);
    }

}

