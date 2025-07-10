package com.example.emergencyassistb4b4.alert.client.user;

import java.util.List;

public interface UserClient {

    List<Long> findUsersByRegion(String province, String city);

}
