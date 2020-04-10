package com.gypaetus.waveform.waveform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Waveform {
    private static Logger LOG = LoggerFactory.getLogger(Waveform.class);

    private static final int DATAPOINTS_PER_SECOND = 10;

    public static short[][] getWaveformFromFile(final File file) throws IOException {
        // use the Java Sound API to extract de audio stream from the file
        final AudioInputStream audioInputStream = createStreamFromFile(file);
        final AudioFormat audioFormat = audioInputStream.getFormat();
        final int numChannels = audioFormat.getChannels();
        final int channelSize = audioFormat.getFrameSize() / audioFormat.getChannels();
        final int bytesPerDatapoint = (int) audioFormat.getFrameRate() / DATAPOINTS_PER_SECOND
                * audioFormat.getFrameSize();
        final int channelLength = bytesPerDatapoint / numChannels;

        final ByteOrder byteOrder = audioFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

        byte[] bytes = new byte[bytesPerDatapoint];

        // Create an ArrayList because the total length is not yet known. Create the channels in this ArrayList.
        ArrayList<ArrayList<Short>> result = new ArrayList<>();
        for (int i = 0; i < numChannels; i++) {
            result.add(i, new ArrayList<>());
        }

        while (audioInputStream.read(bytes) != -1) {
            // Calculate the max value for each channel in the bytes array for this datapoint. Taking the max value (neg
            // or pos) gives the most readable outcome, but this is open for debate.
            for (int channel = 0; channel < numChannels; channel++) {
                int[] channelDataTotal = new int[channelLength];
                int count = 0;
                for (int i = 0; i < channelLength; i += channelSize * numChannels) {
                    final int startByte = i + channel * channelSize;

                    byte[] channelData = Arrays.copyOfRange(bytes, startByte, startByte + channelSize);
                    channelDataTotal[count] = ByteBuffer.wrap(channelData).order(byteOrder).getShort();
                    count++;
                }
                int max = Arrays.stream(channelDataTotal).max().orElse(0);
                int min = Arrays.stream(channelDataTotal).min().orElse(0);
                result.get(channel).add((short) (max > Math.abs(min) ? max : min));
            }
        }

        short[][] toReturn = new short[numChannels][result.get(0).size()];
        for (int channel = 0; channel < numChannels; channel++) {
            for (int datapoint = 0; datapoint < result.get(channel).size(); datapoint++) {
                toReturn[channel][datapoint] = result.get(channel).get(datapoint);
            }
        }

        return toReturn;
    }

    private static AudioInputStream createStreamFromFile(final File file) {
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }

        return audioInputStream;
    }

    private static HashMap<String, Object> getStreamInfo(final AudioInputStream audioInputStream) {
        final HashMap<String, Object> result = new HashMap<>();
        final AudioFormat audioFormat = audioInputStream.getFormat();
        result.put("Summary", audioFormat.toString());
        result.put("Sample Size (bits)", audioFormat.getSampleSizeInBits());
        result.put("Sample Rate", audioFormat.getSampleRate());
        result.put("Encoding", audioFormat.getEncoding());
        result.put("Frame size", audioFormat.getFrameSize());
        result.put("Frame rate", audioFormat.getFrameRate());
        result.put("Big endian", audioFormat.isBigEndian());
        result.put("Frame Length (# in file)", audioInputStream.getFrameLength());

        return result;
    }
}