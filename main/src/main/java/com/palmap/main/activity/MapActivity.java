package com.palmap.main.activity;

import com.google.inject.Inject;

import com.macrowen.macromap.MacroMap;
import com.macrowen.macromap.MacroMap.OnMapEventType;
import com.macrowen.macromap.draw.ShopPosition.OnMapEventListener;
import com.macrowen.macromap.utils.MapService.OnMapFloorChangedListener;
import com.palmap.main.utils.Constant;
import com.palmap.main.utils.WifiPositionController;
import com.palmap.main.utils.WifiPositionController.PositionListenerDelegate;
import com.siemens.wifiposition.Position;
import javax.annotation.Nullable;
import android.widget.ToggleButton;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.content.Intent;
import android.view.MotionEvent;
import roboguice.inject.InjectView;
import android.os.Bundle;

public class MapActivity extends PublicActivity implements PositionListenerDelegate {

  @InjectView(tag = "acntivity_map_layout_macromap_tag")
  @Nullable
  MacroMap mMacroMap;
  @InjectView(tag = "acntivity_map_button_zoomout_tag")
  @Nullable
  ImageButton mZoomout;
  @InjectView(tag = "acntivity_map_button_zoomin_tag")
  @Nullable
  ImageButton mZoomin;
  @InjectView(tag = "acntivity_map_button_position_tag")
  @Nullable
  ToggleButton mPosition;

  // @Inject
  // WifiPositionManager wifiPosManager;

  @Inject
  WifiPositionController wifiController;

  private Handler mHandler = new Handler();

  @Override
  public void onError(int error) {
    mPosition.setChecked(false);
  }

  @Override
  public void onPositionReceived(final Position position, String floorid) {
    Log.w("onPositionReceived", position.x + "_" + position.y);
    Log.w("floorid", floorid);

    mHandler.post(new Runnable() {
      @Override
      public void run() {
        mMapService.setPositionTest("114", position.x, position.y);
      }
    });
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    mMacroMap.dispatchTouchEvent(event);
    return super.onTouchEvent(event);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);
    setTitle("地图");
    enableBack(true);

    mMapService.setViewDelegate(mMacroMap);
    mMacroMap.setMap(mMapService.getMap());

    wifiController.setOnPositionListener(this);

    Intent intent = getIntent();
    processLoaction(intent);

    String shopId = intent.getStringExtra("shopid");
    String floorId = intent.getStringExtra("floorid");
    if ((shopId != null && !shopId.isEmpty()) && (floorId != null && !floorId.isEmpty())) {
      mMapService.setShop(floorId, shopId);
    }

    // String serverIp =
    // getResources().getString(R.string.position_server_adress);
    // int serverProt =
    // Integer.parseInt(getResources().getString(R.string.position_server_port));
    // wifiPosManager.init(serverIp, serverProt, this);
    // wifiPosManager.setPositionInterval(1000);
    //
    // wifiPosManager.setServiceStateListener(new ServiceStateListener() {
    //
    // @Override
    // public void onStateChanged(int arg0) {
    // Log.w("onStateChanged", arg0 + "");
    // }
    // });

    mMapService.setOnMapEventListener(new OnMapEventListener() {
      @Override
      public void OnMapEvent(String id, OnMapEventType type) {
        if (type == OnMapEventType.MapClickedLeft) {
          Intent intent = new Intent(getBaseContext(), DetailActivity.class);
          intent.putExtra("floorid", mMapService.getFloorId());
          intent.putExtra("shopid", "" + id);
          startActivity(intent);
        } else if (type == OnMapEventType.MapClickedRight) {
          Intent intent = new Intent(getBaseContext(), DetailActivity.class);
          intent.putExtra("floorid", mMapService.getFloorId());
          intent.putExtra("shopid", "" + id);
          startActivity(intent);
        }
      }
    });

    mMapService.setOnMapFloorChangedListener(new OnMapFloorChangedListener() {
      @Override
      public void OnMapFloorChanged(String fromFloorid, String toFloorid) {
        wifiController.stop();
      }
    });

    mPosition.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (mPosition.isChecked()) {
          location();
        } else {
          wifiController.stop();
        }
      }
    });
    mZoomin.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mMapService.zoomin();
        // mMapService.getCurFloor().onDrawPositionLayerTest();
      }
    });
    mZoomout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mMapService.zoomout();
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (wifiController.isConnection()) {
      wifiController.stop();
    }
    mMapService.flrushView();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    processLoaction(intent);

    String shopId = intent.getStringExtra("shopid");
    String floorId = intent.getStringExtra("floorid");
    if ((shopId != null && !shopId.isEmpty()) && (floorId != null && !floorId.isEmpty())) {
      mMapService.setShop(floorId, shopId);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (wifiController.isConnection()) {
      wifiController.stop();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (wifiController.isConnection()) {
      mPosition.setChecked(true);
    } else {
      mPosition.setChecked(false);
    }
  }

  private void location() {
    if (wifiController.isConnection()) {
      return;
    }
    wifiController.start();
    // String floorid = "18";
    // float x = 15202;
    // float y = 7447;
    // String floorid = "114";
    // float x = 157;
    // float y = 65;
    // float x = 4046;
    // float y = 1676;
    // float x = 15202;
    // float y = 7447;
    // mMapService.setPosition(floorid, x, y);
  }

  private void processLoaction(Intent intent) {
    String location = intent.getStringExtra(Constant.LOCATION);
    if (location == null || location.equals("")) {
      return;
    }
    location();
  }
}