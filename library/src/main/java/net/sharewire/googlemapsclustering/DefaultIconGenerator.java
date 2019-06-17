package net.sharewire.googlemapsclustering;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.List;

import static net.sharewire.googlemapsclustering.Preconditions.checkNotNull;
import static net.sharewire.googlemapsclustering.R.*;

/**
 * The implementation of {@link IconGenerator} that generates icons with the default style
 * and caches them for subsequent use. To customize the style of generated icons use
 * {@link DefaultIconGenerator#setIconStyle(IconStyle)}.
 */

public class DefaultIconGenerator<T extends ClusterItem> implements IconGenerator<T> {

    private static final int[] CLUSTER_ICON_BUCKETS = {100, 150, 200, 250, 300};

    private final Context mContext;

    private IconStyle mIconStyle;

    private BitmapDescriptor mClusterItemIcon;

    private final SparseArray<BitmapDescriptor> mClusterIcons = new SparseArray<>();

    /**
     * Creates an icon generator with the default icon style.
     */
    public DefaultIconGenerator(@NonNull Context context) {
        mContext = checkNotNull(context);

        //Create Icon Style
        setIconStyle(createDefaultIconStyle());
    }

    /**
     * Sets a custom icon style used to generate marker icons.
     *
     * @param iconStyle the custom icon style used to generate marker icons
     */
    public void setIconStyle(@NonNull IconStyle iconStyle) {
        mIconStyle = checkNotNull(iconStyle);
    }

    @NonNull
    public BitmapDescriptor getClusterIcon(@NonNull Cluster<T> cluster) {
        int clusterBucket = getClusterIconBucket(cluster);
        BitmapDescriptor clusterIcon = mClusterIcons.get(clusterBucket);
        boolean bSamePlace = true;

        int i = 0;
        double lat_0, lon_0, lat, lon;
        List<T> items = cluster.getItems();
        lat_0 = items.get(0).getLatitude();
        lon_0 = items.get(0).getLongitude();
        for( i = 1 ; i < items.size(); i ++ )
        {
            lat = items.get(i).getLatitude();
            lon = items.get(i).getLongitude();
            if( Math.abs(lat - lat_0) > 0.0001 || Math.abs(lon - lon_0) > 0.0001 )
            {
                bSamePlace = false;
                break;
            }
        }
//sgs
        clusterIcon = createClusterIcon(clusterBucket, bSamePlace);
        mClusterIcons.put(clusterBucket, clusterIcon);
        return clusterIcon;
    }

    @NonNull
    @Override
    public BitmapDescriptor getClusterItemIcon(@NonNull T clusterItem) {
        if (mClusterItemIcon == null) {
            mClusterItemIcon = createClusterItemIcon();
        }
        return mClusterItemIcon;
    }

    @NonNull
    private IconStyle createDefaultIconStyle() {
        return new IconStyle.Builder(mContext).build();
    }

    @NonNull
    private BitmapDescriptor createClusterIcon(int clusterBucket, boolean bSamePlace) {
        @SuppressLint("InflateParams")
        TextView clusterIconView = (TextView) LayoutInflater.from(mContext)
                .inflate(R.layout.map_cluster_icon, null);

        clusterIconView.setBackground(createClusterBackground(bSamePlace));
        clusterIconView.setTextColor(mIconStyle.getClusterTextColor());
        clusterIconView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                mIconStyle.getClusterTextSize());

        clusterIconView.setText(getClusterIconText(clusterBucket));

        clusterIconView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        clusterIconView.layout(0, 0, clusterIconView.getMeasuredWidth(),
                clusterIconView.getMeasuredHeight());

        Bitmap iconBitmap = Bitmap.createBitmap(clusterIconView.getMeasuredWidth(),
                clusterIconView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(iconBitmap);
        clusterIconView.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(iconBitmap);
    }

    @NonNull
    private Drawable createClusterBackground(boolean bSamePlace) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);

        gradientDrawable.setColor(mIconStyle.getClusterBackgroundColor(bSamePlace));
        gradientDrawable.setStroke(mIconStyle.getClusterStrokeWidth(),
                mIconStyle.getClusterStrokeColor());
        return gradientDrawable;
    }

    @NonNull
    private BitmapDescriptor createClusterItemIcon() {
        return BitmapDescriptorFactory.fromResource(mIconStyle.getClusterIconResId());
    }

    private int getClusterIconBucket(@NonNull Cluster<T> cluster) {
        int itemCount = cluster.getItems().size();
        if (itemCount <= CLUSTER_ICON_BUCKETS[0]) {
            return itemCount;
        }

        for (int i = 0; i < CLUSTER_ICON_BUCKETS.length - 1; i++) {
            if (itemCount < CLUSTER_ICON_BUCKETS[i + 1]) {
                return CLUSTER_ICON_BUCKETS[i];
            }
        }

        return CLUSTER_ICON_BUCKETS[CLUSTER_ICON_BUCKETS.length - 1];
    }

    @NonNull
    private String getClusterIconText(int clusterIconBucket) {
        return (clusterIconBucket < CLUSTER_ICON_BUCKETS[0]) ?
                String.valueOf(clusterIconBucket) : String.valueOf(clusterIconBucket) + "+";
    }
}
