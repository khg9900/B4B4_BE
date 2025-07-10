package com.example.emergencyassistb4b4.alert.client.location;

import com.example.emergencyassistb4b4.location.service.LocationService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationClientImpl implements LocationClient {

    private final LocationService locationService;

    @Override
    public List<Long> findUsersByRegion(String province, String city) {

        String regionKey = String.format("region:%s:%s", province, city);
        Set<Object> users = locationService.getRegion(regionKey);

        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }

        return users.stream()
            .map(String::valueOf)
            .map(Long::valueOf)
            .toList();
    }
}
