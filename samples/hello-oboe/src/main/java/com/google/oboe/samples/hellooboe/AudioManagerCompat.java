package com.google.oboe.samples.hellooboe;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


public abstract class AudioManagerCompat {
    private static final String TAG = AudioManagerCompat.class.getSimpleName();

    protected final AudioManager audioManager;
    private static final int AUDIOFOCUS_GAIN = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;

    private static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;

    public static AudioManager getAudioManager (@NonNull Context context) {
        return (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }

    private AudioManagerCompat(@NonNull Context context) {
        audioManager = getAudioManager(context);

        onAudioFocusChangeListener = focusChange -> {
            String focusEvent = "UNKNOWN";
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    focusEvent = "AUDIOFOCUS_GAIN";
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    focusEvent = "AUDIOFOCUS_GAIN_TRANSIENT";
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    focusEvent = "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    focusEvent = "AUDIOFOCUS_LOSS";
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    focusEvent = "AUDIOFOCUS_LOSS_TRANSIENT";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    focusEvent = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    break;
            }
            Log.d(TAG, "onAudioFocusChange -> "+focusEvent);
        };
    }

    abstract public void requestCallAudioFocus();
    abstract public void abandonCallAudioFocus();

    public static AudioManagerCompat create(@NonNull Context context) {
        return new Api26AudioManagerCompat(context);
    }

    private static class Api26AudioManagerCompat extends AudioManagerCompat {

        private static AudioAttributes AUDIO_ATTRIBUTES = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setLegacyStreamType(AudioManager.STREAM_VOICE_CALL)
                .build();

        private AudioFocusRequest audioFocusRequest;

        private Api26AudioManagerCompat(@NonNull Context context) {
            super(context);
        }

        @Override
        public void requestCallAudioFocus() {
            if (audioFocusRequest != null) {
                Log.i(TAG, "requestCallAudioFocus ignoring...");
                return;
            }

            audioFocusRequest = new AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                    .setAudioAttributes(AUDIO_ATTRIBUTES)
                    .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
                    .build();

            int result = audioManager.requestAudioFocus(audioFocusRequest);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.w(TAG, "requestCallAudioFocus failed: " + result);
            }
        }

        @Override
        public void abandonCallAudioFocus() {
            if (audioFocusRequest == null) {
                Log.i(TAG, "abandonCallAudioFocus ignoring...");
                return;
            }

            int result = audioManager.abandonAudioFocusRequest(audioFocusRequest);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.w(TAG, "abandonCallAudioFocus failed: " + result);
            }

            audioFocusRequest = null;
        }
    }
//endregion API26

//region API21
    @RequiresApi(21)
    private static class Api21AudioManagerCompat extends AudioManagerCompat {

        private static AudioAttributes AUDIO_ATTRIBUTES = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setLegacyStreamType(AudioManager.STREAM_VOICE_CALL)
                .build();

        private Api21AudioManagerCompat(@NonNull Context context) {
            super(context);
        }

        @Override
        public void requestCallAudioFocus() {
            int result = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_VOICE_CALL, AUDIOFOCUS_GAIN);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.w(TAG, "requestCallAudioFocus failed: " + result);
            }
        }

        @Override
        public void abandonCallAudioFocus() {
            int result = audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.w(TAG, "abandonCallAudioFocus failed: " + result);
            }

        }
    }
//endregion API21
}
