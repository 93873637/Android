package com.liz.androidutils;

import android.location.Location;

import java.text.DecimalFormat;

public class LocationUtils {

    public static final double EARTH_RADIUS_EQUATOR = 6378137;
    public static final double EARTH_RADIUS_AVERAGE = 6371393;

    public static double toKMH(double ms) {
        return ms * 3600 / 1000;
    }

    public static double getDistance(Location loc1, Location loc2) {
        return getDistance(loc1.getLongitude(), loc1.getLatitude(),
                loc2.getLongitude(), loc2.getLatitude());
    }

    public static double getDistance(double lng1, double lat1, double lng2, double lat2) {
        return getDistanceAsin(lng1, lat1, lng2, lat2);
    }

    public static double getDistanceAsin(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS_EQUATOR;
        return s;
    }

    public static double getDistanceAcos(double lng1, double lat1, double lng2, double lat2) {
        double radAX = Math.toRadians(lng1);
        double radAY = Math.toRadians(lat1);
        double radBX = Math.toRadians(lng2);
        double radBY = Math.toRadians(lat2);
        double cos = Math.cos(radAY) * Math.cos(radBY) * Math.cos(radAX - radBX)
                + Math.sin(radAY) * Math.sin(radBY);
        double acos = Math.acos(cos);
        return EARTH_RADIUS_AVERAGE * acos;
    }

    public static double getAngle(double lng1, double lat1, double lng2,
                                  double lat2) {
        double fLat = Math.PI * (lat1) / 180.0;
        double fLng = Math.PI * (lng1) / 180.0;
        double tLat = Math.PI * (lat2) / 180.0;
        double tLng = Math.PI * (lng2) / 180.0;
        double degree = (Math.atan2(Math.sin(tLng - fLng) * Math.cos(tLat), Math.cos(fLat) * Math.sin(tLat) - Math.sin(fLat) * Math.cos(tLat) * Math.cos(tLng - fLng))) * 180.0 / Math.PI;
        if (degree >= 0) {
            return degree;
        } else {
            return 360 + degree;
        }
    }

    public static String formatDistance(double distance) {
        if (distance == 0) {
            return "0 m";
        }
        else if (distance < 1) {
            return new DecimalFormat("0.00").format(distance) + " m";
        }
        else if (distance < 1000*10) {
            return new DecimalFormat("#,###").format(distance) + " m";
        }
        else {
            return new DecimalFormat("###,##0.00").format(distance/1000) + " km";
        }
    }

    public static String getDualSpeedText(double speed) {
        return String.format("%.2f", speed)
                + "/"
                + String.format("%.2f", LocationUtils.toKMH(speed))
                ;
    }

    ////////////////////////////////////////////////////////////////////////////////////////
//        GpsStatus.Listener gpsS = new GpsStatus.Listener() {
//            @Override
//            public void onGpsStatusChanged(int event) {
//                gpscount = 0;
//// TODO Auto-generated method stub
//                if(event== GpsStatus.GPS_EVENT_FIRST_FIX){
//     //第一次定位
//   }else if(event==GpsStatus.GPS_EVENT_SATELLITE_STATUS){
//     //卫星状态改变
//     GpsStatus gpsStauts= locationManager.getGpsStatus(null); // 取当前状态
//     int maxSatellites = gpsStauts.getMaxSatellites(); //获取卫星颗数的默认最大值
//
//     Iterator<GpsSatellite> it = gpsStauts.getSatellites().iterator();//创建一个迭代器保存所有卫星
//     while (it.hasNext() && gpscount <= maxSatellites) {
//       GpsSatellite s = it.next();
//       //可见卫星数量
//       if(s.usedInFix()){
//       //已定位卫星数量
//       gpscount++;
//       }
//     }
//     //gpsCount.Gpscount(gpscount);
//
//   }else if(event==GpsStatus.GPS_EVENT_STARTED){
//     //定位启动
//   }else if(event==GpsStatus.GPS_EVENT_STOPPED){
//     //定位结束
//   }
//            }
//        };
//
//
//
//        //代码里面有一个已定位的卫星判断，这个方法返回的状态值就是
//
////        if(s.usedInFix()){
////       //已定位卫星数量
////       gpscount++;
////       }
    ////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {
        JLog.d("Test Start...\n");

        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(0)", "0 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(0.2)", "0.20 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(0.12345678)", "0.12 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(1)", "1 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(123)", "123 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(1000)", "1,000 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(9876)", "9,876 m");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(98765)", "98.77 km");
        AssertUtils.assertEquals("com.liz.androidutils.LocationUtils.formatDistance(10000)", "10.00 km");
        //test_items();

        JLog.d("Test Successfully.\n");
    }

    public static void test_items() {
        // data retrieved from http://www.box3.cn/tools/lbs.html
        test_item(49.028399510054, 8.3575328887407, 49.028379740816, 8.3576200380035);  //10
        test_item(113.928425, 22.526803, 113.925665, 22.524073);	//415
        test_item(49.028399510054, 8.3575328887407, 49.028379740816, 8.3576200380035);	//	10
        test_item(112.66175548104307, 34.75734244194438,	113.66175548104307, 34.75734244194438);	//	91297
        test_item(120.237629, 29.314884,	120.255522, 29.311574);	//	1772
        test_item(120.237629, 29.314884,	120.237551, 29.315009);	//	16
        test_item(108.83255626, 34.20054047,	108.83262616, 34.20053934);	//	6
        test_item(116.3971280, 39.9165270, 116.3971480, 99.9163270);	//	6667485
        test_item(87.105, 42.917,	87.205, 42.504);	//	46615
    }

    public static void test_item(double lng1, double lat1, double lng2, double lat2) {
        System.out.println("\n");
        System.out.println("getDistance(" + lng1 + ", " + lat1 + ", " + lng2 + ", " + lat2 + ") = " + getDistance(lng1, lat1, lng2, lat2));
        System.out.println("getDistanceAsin(" + lng1 + ", " + lat1 + ", " + lng2 + ", " + lat2 + ") = " + getDistanceAsin(lng1, lat1, lng2, lat2));
        System.out.println("getDistanceAcos(" + lng1 + ", " + lat1 + ", " + lng2 + ", " + lat2 + ") = " + getDistanceAcos(lng1, lat1, lng2, lat2));
        System.out.println("getAngle(" + lng1 + ", " + lat1 + ", " + lng2 + ", " + lat2 + ") = " + getAngle(lng1, lat1, lng2, lat2));
        System.out.println("\n");
    }
}
