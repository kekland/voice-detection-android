package com.kekland.voicedetection;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.Voice;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Build.VERSION_CODES.N;

/**
 * Created by kkerz on 30-May-18.
 */

public class VoiceRecorderThread extends Thread {

    VoiceRecorderThread(Context ctx) {
        context = ctx;
    }
    private Context context;
    private boolean stopped = false;
    private final int bufferLength = 1024;
    private final int sampleRate = 44100;
    private final int channelFormat = AudioFormat.CHANNEL_IN_MONO;
    private final int encodingType = AudioFormat.ENCODING_PCM_16BIT;
    @Override
    public void run() {
        Looper.prepare();
        Log.i("AudioRecorder", "Starting Audio Thread");

        AudioRecord recorder = null;
        //AudioTrack track = null;
        short[] buffer = new short[bufferLength];

        try {
            int N = AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            recorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    N * 10);

           /*track = new AudioTrack(
                   AudioManager.STREAM_MUSIC,
                   sampleRate,
                   AudioFormat.CHANNEL_OUT_MONO,
                   AudioFormat.ENCODING_PCM_16BIT,
                   N*10,
                   AudioTrack.MODE_STREAM
           );*/

            recorder.startRecording();
            //track.play();

            long time = System.currentTimeMillis();
            long timeSpeechStarted = System.currentTimeMillis();
            long timeSpeechStopped = System.currentTimeMillis();
            boolean isSpeakingTruly = false;
            boolean speechDetectedBefore = false;
            while(!stopped) {
                //Log.i("AudioRecorder", "Writing data to buffer");
                N = recorder.read(buffer, 0, buffer.length);
                final boolean speechDetected = processData(normalizePCMBuffer(buffer));
                time = System.currentTimeMillis();

                if(speechDetected && !speechDetectedBefore) {
                    if(isSpeakingTruly) {
                        speechDetectedBefore = true;
                        isSpeakingTruly = true;
                    }
                    else {
                        speechDetectedBefore = true;
                        timeSpeechStarted = time;
                        isSpeakingTruly = false;
                    }
                }
                else if(speechDetected && speechDetectedBefore) {
                    if(time - timeSpeechStarted > 100 && !isSpeakingTruly) {
                        isSpeakingTruly = true;
                        Log.i("AudioRecorder", "Someone started speaking");
                    }
                }
                else if(!speechDetected && speechDetectedBefore){
                    speechDetectedBefore = false;
                    timeSpeechStopped = System.currentTimeMillis();
                }
                else if(!speechDetected && !speechDetectedBefore) {
                    if(time - timeSpeechStopped > 500 && isSpeakingTruly) {
                        isSpeakingTruly = false;
                        Log.i("AudioRecorder", "Someone stopped speaking for period of " + (timeSpeechStopped - timeSpeechStarted) + "ms");
                    }
                }
            }
        }
        catch(Throwable e) {
            Log.w("AudioRecorder", "Error reading voice audio", e);
        }
        finally {
            recorder.stop();
            recorder.release();
            //track.stop();
            //track.release();
        }
    }

    private static float voiceThresholdValue = 0.25f;
    private FastFourierTransform fastFourierTransform = new FastFourierTransform(bufferLength,  sampleRate);
    boolean processData(final float[] buffer) {
        fastFourierTransform.forward(buffer);
        float medianValueInRangeMale = 0f;
        float medianValueInRangeFemale = 0f;
        for(int i = 0; i < 20000; i++) {
            if(i > 85 && i < 180) {
                medianValueInRangeMale += fastFourierTransform.getFreq(i);
            }
            if(i > 165 && i < 255) {
                medianValueInRangeFemale += fastFourierTransform.getFreq(i);
            }
        }
        medianValueInRangeMale /= 95f;
        medianValueInRangeFemale /= 90f;

        if(medianValueInRangeMale > voiceThresholdValue || medianValueInRangeFemale > voiceThresholdValue) {
            return true;
        }
        return false;
    }
    private float shortMaxValue = Short.MAX_VALUE;
    private float[] normalizePCMBuffer(short[] buffer) {
        float[] normalizedBuffer = new float[buffer.length];

        for(int bufferIndex = 0; bufferIndex < buffer.length; bufferIndex++) {
            short bufferValue = buffer[bufferIndex];
            float normalizedBufferValue = (float)bufferValue / shortMaxValue;

            if(normalizedBufferValue > 1f) {
                normalizedBufferValue = 1f;
            }
            else if(normalizedBufferValue < -1f) {
                normalizedBufferValue = -1f;
            }

            normalizedBuffer[bufferIndex] = normalizedBufferValue;
        }

        return normalizedBuffer;
    }


    public void close()
    {
        stopped = true;
    }
}
