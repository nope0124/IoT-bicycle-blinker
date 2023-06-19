package com.example.webacrivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import com.example.webacrivity.HttpGetTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
//    private TextView mReturnTextView;
//    private EditText mEditTextName;
//
//    private TextView locationText;
    private LocationManager locationManager;

    // Button
    private Button mGetRoutesButton;

    // EditText
    private EditText mDestinationEditText;

    // TextView
    private TextView mRoutesTextView;
    private TextView mOriginLatitudeTextView;
    private TextView mOriginLongitudeTextView;
    private TextView mDestinationLatitudeTextView;
    private TextView mDestinationLongitudeTextView;

    // MainActivity
    private MainActivity activity = this;

    private boolean flag = true;

    private static final int PERMISSIONS_REQUEST = 1;

    private TextView mDestinationTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Button
        Button mGetRoutesButton = (Button) findViewById(R.id.button_get_routes);
        mGetRoutesButton.setOnClickListener(this);

        // EditText
        mDestinationEditText = (EditText) findViewById(R.id.edit_text_destination);

        // TextView
        mRoutesTextView = (TextView) findViewById(R.id.text_view_routes);
        mOriginLatitudeTextView = (TextView) findViewById(R.id.text_view_origin_latitude);
        mOriginLongitudeTextView = (TextView) findViewById(R.id.text_view_origin_longitude);
        mDestinationLatitudeTextView = (TextView) findViewById(R.id.text_view_destination_latitude);
        mDestinationLongitudeTextView = (TextView) findViewById(R.id.text_view_destination_longitude);

    }

    @Override
    public void onClick(View view) {
        Log.d("WebActivity", "wlog onClick()");
        if(flag) {
            HttpGetTask task = new HttpGetTask(activity, mRoutesTextView, mDestinationEditText, mDestinationLatitudeTextView, mDestinationLongitudeTextView, 35.663546, 140.079379);
            task.execute();
            flag = false;
        }
//        getLocation();
//        HttpGetTask task = new HttpGetTask(this, mReturnTextView, mEditTextName);
//
//        if (view.getId() == R.id.button_html) {
//            task.execute();
//        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            System.out.println(100);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
            System.out.println(100);
        } else {
            System.out.println(200);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            System.out.println(200);
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            mOriginLatitudeTextView.setText("現在地緯度: " + location.getLatitude());
            mOriginLongitudeTextView.setText("現在地経度: " + location.getLongitude());
            if(flag) {
                HttpGetTask task = new HttpGetTask(activity, mRoutesTextView, mDestinationEditText, mDestinationLatitudeTextView, mDestinationLongitudeTextView, location.getLatitude(), location.getLongitude());
                task.execute();
                flag = false;
            }
//            Point nearestPoint = findNearestPoint(location, points);
//            if (nearestPoint != null) {
//                double distanceToNearestPoint = location.distanceTo(nearestPoint.getLocation()) / 1000;
//                String directionToNearestPoint = bearingToDirection(calculateBearing(location.getLatitude(), location.getLongitude(), nearestPoint.getLatitude(), nearestPoint.getLongitude()));
//                nearestPointText.setText("最も近い公園: " + nearestPoint.getName() + ", " + String.format("%.1f", distanceToNearestPoint) + " km, 方角: " + directionToNearestPoint);
//            }
//
//            List<Point> nearbyPoints = findNearbyPoints(location, points, 5);
//            StringBuilder nearbyPointsStr = new StringBuilder("5km以内の公園:\n");
//            for (Point point : nearbyPoints) {
//                if (!point.equals(nearestPoint)) {
//                    double distance = location.distanceTo(point.getLocation()) / 1000;
//                    String direction = bearingToDirection(calculateBearing(location.getLatitude(), location.getLongitude(), point.getLatitude(), point.getLongitude()));
//                    nearbyPointsStr.append(point.getName()).append(": ").append(String.format("%.1f", distance)).append(" km, 方角: ").append(direction).append("\n");
//                }
//            }
//            nearbyPointsText.setText(nearbyPointsStr.toString());
        }

//        private Point findNearestPoint(Location location, List<Point> points) {
//            Point nearestPoint = null;
//            double shortestDistance = Double.MAX_VALUE;
//
//            for (Point point : points) {
//                double distance = location.distanceTo(point.getLocation());
//                if (distance < shortestDistance) {
//                    nearestPoint = point;
//                    shortestDistance = distance;
//                }
//            }
//            return nearestPoint;
//        }

//        private List<Point> findNearbyPoints(Location location, List<Point> points, double maxDistanceInKm) {
//            List<Point> nearbyPoints = new ArrayList<>();
//
//            for (Point point : points) {
//                double distance = location.distanceTo(point.getLocation()) / 1000;
//                if (distance <= maxDistanceInKm) {
//                    nearbyPoints.add(point);
//                }
//            }
//            Collections.sort(nearbyPoints, new Comparator<Point>() {
//                @Override
//                public int compare(Point p1, Point p2) {
//                    double distanceToP1 = location.distanceTo(p1.getLocation());
//                    double distanceToP2 = location.distanceTo(p2.getLocation());
//                    return Double.compare(distanceToP1, distanceToP2);
//                }
//            });
//
//            return nearbyPoints;
//        }

//        private double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
//            double longitudeDiff = Math.toRadians(lon2 - lon1);
//            double lat1Rad = Math.toRadians(lat1);
//            double lat2Rad = Math.toRadians(lat2);
//            double y = Math.sin(longitudeDiff) * Math.cos(lat2Rad);
//            double x = Math.cos(lat1Rad)*Math.sin(lat2Rad) - Math.sin(lat1Rad)*Math.cos(lat2Rad)*Math.cos(longitudeDiff);
//            return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
//        }

//        private String bearingToDirection(double bearing) {
//            String[] directions = {"北", "北北東", "北東", "東北東", "東", "東南東", "南東", "南南東", "南", "南南西", "南西", "西南西", "西", "西北西", "北西", "北北西", "北"};
//            return directions[(int)Math.round(((bearing % 360) / 22.5))];
//        }

        public void onStatusChanged(String provider, int status, Bundle extras) { }

        public void onProviderEnabled(String provider) { }

        public void onProviderDisabled(String provider) { }
    };


}