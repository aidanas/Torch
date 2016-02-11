package com.aidanas.torch;

import android.hardware.Camera;

/**
 * Created by: Aidanas.
 * Created on: on 11/02/2016.
 *
 * Classes implementing this interface must be able to provide fully activated Camera object on
 * request.
 */
public interface CameraProvider {
    Camera getDeviceCamera();
}
