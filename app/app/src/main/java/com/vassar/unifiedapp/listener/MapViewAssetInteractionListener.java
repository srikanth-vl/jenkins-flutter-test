//package com.vassar.unifiedapp.listener;
//
//import android.content.Context;
//import android.view.MotionEvent;
//
//import com.esri.arcgisruntime.concurrent.ListenableFuture;
//import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
//import com.esri.arcgisruntime.mapping.view.Graphic;
//import com.esri.arcgisruntime.mapping.view.MapView;
//
//import java.util.List;
//
//public class MapViewAssetInteractionListener extends DefaultMapViewOnTouchListener {
//
//    private MapView mMapView;
//
//    public MapViewAssetInteractionListener(Context context, MapView mapView) {
//        super(context, mapView);
//        mMapView = mapView;
//    }
//
//    @Override
//    public boolean onSingleTapConfirmed(MotionEvent e) {
//        //get the tapped location in screen coordinates.
////        android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(),
////                (int) e.getY());
//        //use the screen location to perform the identify
////        final ListenableFuture<List<Graphic>> identifyGraphics = mMapView
////                .identifyGraphicsOverlayAsync(graphicsOverlay,
////                screenPoint, 10, 2);
//        //wait for the results to be returned.
////        identifyGraphics.addDoneListener(new Runnable() {
////            @Override
////            public void run() {
////                seaBirdDialog(identifyGraphics);
////            }
////        });
//        return true;
//    }
//}
