package com.rol.beans;

import java.io.Serializable;

/**
 * Created by keshav on 5/9/18.
 */

public class UpcomingTaskBean implements Serializable {

    public String task_id;
    public String organiser_id;
    public String organiser_name;
    public String organiser_image;
    public String task_name;
    public String task_image;
    public String location;
    public String created_date;
    public String start_date;
    public String start_time;
    public String no_of_helper;
    public String distance;
    public String description;
    public String total_helper;
    public String task_status;

    public String is_my_added;
}
