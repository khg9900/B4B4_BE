package com.example.emergencyassistb4b4.alert.client.location;

import java.util.List;

public interface LocationClient {

    List<Long> findUsersByRegion(String province, String city);

}
