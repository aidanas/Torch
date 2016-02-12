package com.aidanas.torch.interfaces;

import android.hardware.Camera;

/**
 * Created by: Aidanas.
 * Created on: on 11/02/2016.
 *
 * Classes implementing this interface must be able to provide fully activated Camera object on
 * request.
 */
public interface CameraProvider {

    /**
     * Get a camera object.
     * @return - Camera.
     */
    Camera getDeviceCamera();

    /**
     * Signals then camera not needed anymore. It is up to the implementation of the interface
     * whether it actually gets released or not.
     *
     * @param - Camera to be released.
     */
    void releaseCamera(Camera cam);

}
