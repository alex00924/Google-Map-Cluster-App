package com.rol;

import java.util.ArrayList;
import java.util.List;

public class Global {
    public static List<Integer> m_arrState = new ArrayList<>();

    public static final int           TASK_LIST =          1;
    public static final int           TASK_MAP =           2;
    public static final int           TASK_MY =            3;
    public static final int           FRIEND_DISCOVER =    4;
    public static final int           FRIEND_REQUEST =     5;
    public static final int           FRIEND_MY =          6;

    public static final int           TASK =                10;
    public static final int           FRIEND =              20;
    public static int                 g_nCurTag = 0;

}
