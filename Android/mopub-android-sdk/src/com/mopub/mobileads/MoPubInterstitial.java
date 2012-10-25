/*
 * Copyright (c) 2011, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads;

import java.util.Map;

import com.mopub.mobileads.BaseInterstitialAdapter.BaseInterstitialAdapterListener;
import com.mopub.mobileads.MoPubView.LocationAwareness;
import com.mopub.mobileads.MoPubView.OnAdFailedListener;
import com.mopub.mobileads.MoPubView.OnAdLoadedListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

public class MoPubInterstitial implements OnAdLoadedListener, OnAdFailedListener {
    
    private enum InterstitialState { HTML_AD_READY, NATIVE_AD_READY, NOT_READY };
    
    private MoPubInterstitialView mInterstitialView;
    private BaseInterstitialAdapter mInterstitialAdapter;
    private MoPubInterstitialListener mListener;
    private Activity mActivity;
    private String mAdUnitId;
    private BaseInterstitialAdapterListener mAdapterListener;
    private DefaultInterstitialAdapterListener mDefaultAdapterListener;
    private InterstitialState mCurrentInterstitialState;
    
    public interface MoPubInterstitialListener {
        public void OnInterstitialLoaded();
        public void OnInterstitialFailed();
    }
    
    public MoPubInterstitial(Activity activity, String id) {
        mActivity = activity;
        mAdUnitId = id;
        
        mInterstitialView = new MoPubInterstitialView(mActivity);
        mInterstitialView.setAdUnitId(mAdUnitId);
        
        mCurrentInterstitialState = InterstitialState.NOT_READY;
        mDefaultAdapterListener = new DefaultInterstitialAdapterListener();
        mAdapterListener = mDefaultAdapterListener;
    }

    public void load() {
        invalidateCurrentInterstitial();
        mInterstitialView.loadAd();
    }
    
    public void forceRefresh() {
        invalidateCurrentInterstitial();
        mInterstitialView.forceRefresh();
    }
    
    private void invalidateCurrentInterstitial() {
        mCurrentInterstitialState = InterstitialState.NOT_READY;
        
        if (mInterstitialAdapter != null) {
            mInterstitialAdapter.invalidate();
            mInterstitialAdapter = null;
        }
        
        mAdapterListener = mDefaultAdapterListener;
        
        mInterstitialView.setOnAdLoadedListener(this);
        mInterstitialView.setOnAdFailedListener(this);
    }
    
    public boolean isReady() {
        return (mCurrentInterstitialState == InterstitialState.HTML_AD_READY) || 
                (mCurrentInterstitialState == InterstitialState.NATIVE_AD_READY);
    }
    
    public boolean show() {
        switch (mCurrentInterstitialState) {
            case HTML_AD_READY: showHtmlInterstitial(); return true;
            case NATIVE_AD_READY: showNativeInterstitial(); return true;
            default: return false;
        }
    }
    
    private void showHtmlInterstitial() {
        String responseString = mInterstitialView.getResponseString();
        Intent i = new Intent(mActivity, MoPubActivity.class);
        i.putExtra("com.mopub.mobileads.AdUnitId", mAdUnitId);
        i.putExtra("com.mopub.mobileads.Keywords", mInterstitialView.getKeywords());
        i.putExtra("com.mopub.mobileads.Source", responseString);
        i.putExtra("com.mopub.mobileads.ClickthroughUrl", mInterstitialView.getClickthroughUrl());
        mActivity.startActivity(i);
    }
    
    private void showNativeInterstitial() {
        if (mInterstitialAdapter != null) mInterstitialAdapter.showInterstitial();
    }
    
    @Override
    public void OnAdFailed(MoPubView m) {
        mCurrentInterstitialState = InterstitialState.NOT_READY;
        if (mListener != null) mListener.OnInterstitialFailed();
    }

    @Override
    public void OnAdLoaded(MoPubView m) {
        mCurrentInterstitialState = InterstitialState.HTML_AD_READY;
        
        if (mInterstitialAdapter != null) {
            mInterstitialAdapter.invalidate();
            mInterstitialAdapter = null;
        }
        
        if (mListener != null) mListener.OnInterstitialLoaded();
    }
    
    AdView getAdView() {
        return mInterstitialView.mAdView;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void setKeywords(String keywords) {
        if (mInterstitialView != null) mInterstitialView.setKeywords(keywords);
    }

    public String getKeywords() {
        return (mInterstitialView != null) ? mInterstitialView.getKeywords() : null;
    }
    
    public Activity getActivity() {
    	return mActivity;
    }
    
    public void setListener(MoPubInterstitialListener listener) {
        mListener = listener;
    }
    
    public MoPubInterstitialListener getListener() {
        return mListener;
    }
    
    public Location getLocation() {
        return mInterstitialView.getLocation();
    }
    
    public void destroy() {
        mAdapterListener = null;
        
        if (mInterstitialAdapter != null) {
            mInterstitialAdapter.invalidate();
            mInterstitialAdapter = null;
        }
        
        mInterstitialView.setOnAdLoadedListener(null);
        mInterstitialView.setOnAdFailedListener(null);
        mInterstitialView.destroy();
    }
    
    public void setLocationAwareness(LocationAwareness awareness) {
        mInterstitialView.setLocationAwareness(awareness);
    }

    public LocationAwareness getLocationAwareness() {
        return mInterstitialView.getLocationAwareness();
    }

    public void setLocationPrecision(int precision) {
        mInterstitialView.setLocationPrecision(precision);
    }

    public int getLocationPrecision() {
        return mInterstitialView.getLocationPrecision();
    }
    
    protected BaseInterstitialAdapterListener getInterstitialAdapterListener () {
        return mAdapterListener;
    }
    
    public void setTesting(boolean testing) {
        mInterstitialView.setTesting(testing);
    }
    
    public boolean getTesting() {
        return mInterstitialView.getTesting();
    }
    
    public void setLocalExtras(Map<String, Object> extras) {
        mInterstitialView.setLocalExtras(extras);
    }
    
    public Map<String, Object> getLocalExtras() {
        return mInterstitialView.getLocalExtras();
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    public class DefaultInterstitialAdapterListener implements BaseInterstitialAdapterListener {
        public void onNativeInterstitialLoaded(BaseInterstitialAdapter adapter) {
            mCurrentInterstitialState = InterstitialState.NATIVE_AD_READY;
            mInterstitialView.trackImpression();
            if (mListener != null) mListener.OnInterstitialLoaded();
        }

        public void onNativeInterstitialFailed(BaseInterstitialAdapter adapter) {
            mCurrentInterstitialState = InterstitialState.NOT_READY;
            mInterstitialView.loadFailUrl();
        }

        public void onNativeInterstitialClicked(BaseInterstitialAdapter adapter) {
            mInterstitialView.registerClick();
        }
        
        public void onNativeInterstitialExpired(BaseInterstitialAdapter adapter) {
            mCurrentInterstitialState = InterstitialState.NOT_READY;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    public class MoPubInterstitialView extends MoPubView {
        
        public MoPubInterstitialView(Context context) {
            super(context);
            setAutorefreshEnabled(false);
        }
        
        @Override
        protected void loadNativeSDK(Map<String, String> paramsMap) {
            if (paramsMap == null) return;
            
            MoPubInterstitial interstitial = MoPubInterstitial.this;
            BaseInterstitialAdapterListener adapterListener = 
                interstitial.getInterstitialAdapterListener();
            String type = paramsMap.get("X-Adtype");
            
            if (type != null && (type.equals("interstitial") || type.equals("mraid"))) {
                String interstitialType = type.equals("interstitial") ? 
                        paramsMap.get("X-Fulladtype") : "mraid";
                
                Log.i("MoPub", "Loading native adapter for interstitial type: " + interstitialType);
                mInterstitialAdapter =
                        BaseInterstitialAdapter.getAdapterForType(interstitialType);
                
                if (mInterstitialAdapter != null) {
                    String jsonParams = paramsMap.get("X-Nativeparams");
                    mInterstitialAdapter.init(interstitial, jsonParams);
                    mInterstitialAdapter.setAdapterListener(adapterListener);
                    mInterstitialAdapter.loadInterstitial();
                    return;
                }
            }
            
            Log.i("MoPub", "Couldn't load native adapter. Trying next ad...");
            adapterListener.onNativeInterstitialFailed(null);
        }
        
        @Override
        protected void loadCustomEvent(Map<String, String> paramsMap) {
            if (mInterstitialAdapter != null) mAdapter.invalidate();
            
            mInterstitialAdapter = BaseInterstitialAdapter.getAdapterForType("custom_event");
            
            if (mInterstitialAdapter != null) {
                Log.i("MoPub", "Loading custom event interstitial adapter.");
                
                // Get the className and classData from the passed in paramsMap.
                String className = paramsMap.get("X-Custom-Event-Class-Name");
                String classData = paramsMap.get("X-Custom-Event-Class-Data");
                
                ((CustomEventInterstitialAdapter) mInterstitialAdapter)
                        .init(MoPubInterstitial.this, className, classData);
                
                mInterstitialAdapter.loadInterstitial();
            } else {
                Log.i("MoPub", "Couldn't load custom event interstitial adapter. Trying next ad...");
                loadFailUrl();
            }
        }
        
        protected void trackImpression() {
            Log.d("MoPub", "Tracking impression for interstitial.");
            if (mAdView != null) mAdView.trackImpression();
        }
    }
    
    @Deprecated
    public void customEventDidLoadAd() {
        if (mInterstitialView != null) mInterstitialView.trackImpression();
    }

    @Deprecated
    public void customEventDidFailToLoadAd() {
        if (mInterstitialView != null) mInterstitialView.loadFailUrl();
    }

    @Deprecated
    public void customEventActionWillBegin() {
        if (mInterstitialView != null) mInterstitialView.registerClick();
    }

    @Deprecated
    public void showAd() {
        /* 
         * To show the ad immediately upon loading, we need to register a new OnAdLoadedListener,
         * as well as a new interstitial adapter listener.
         */
        
        mAdapterListener = new DefaultInterstitialAdapterListener() {
            public void onNativeInterstitialLoaded(BaseInterstitialAdapter adapter) {
                super.onNativeInterstitialLoaded(adapter);
                MoPubInterstitial.this.show();
            }
        };
        
        mInterstitialView.setOnAdLoadedListener(new OnAdLoadedListener() {
            public void OnAdLoaded(MoPubView m) {
                MoPubInterstitial.this.OnAdLoaded(m);
                MoPubInterstitial.this.show();
            }
        });
        
        mInterstitialView.loadAd();
    }
}
