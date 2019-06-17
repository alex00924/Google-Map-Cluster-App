package com.rol.adapter;

public interface RefreshDataCallback {
    void refreshData(int nPos, int nKind, String strVal);  //nKind : 0 : fav, 1 : apply
}