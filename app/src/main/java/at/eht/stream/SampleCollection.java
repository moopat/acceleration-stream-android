package at.eht.stream;

/**
 * Stores Samples in memory.
 *
 * MAX_RETENTION_COUNT is the max number of Samples stored. If more samples are added
 * the oldest are removed.
 *
 * @author Markus Deutsch
 */
public class SampleCollection {

    private static SampleCollection mInstance;
    public static final int MAX_RETENTION_COUNT = 500;

    private Sample[] collection;
    private int nextWritePosition = 0;

    private SampleCollection(){
        collection = new Sample[MAX_RETENTION_COUNT];
    }

    public static SampleCollection getInstance(){
        if(mInstance == null) mInstance = new SampleCollection();
        return mInstance;
    }

    public void insert(Sample[] data){
        if(data.length > MAX_RETENTION_COUNT)
            throw new IllegalArgumentException("The input array must not be larger than " + MAX_RETENTION_COUNT + ".");

        // Is there enough space?
        if(nextWritePosition + data.length > MAX_RETENTION_COUNT){
            delete(nextWritePosition + data.length - MAX_RETENTION_COUNT);
        }

        for (int i = 0; i < data.length; i++) {
            collection[nextWritePosition] = data[i];
            nextWritePosition++;
        }
    }

    public void delete(int numberOfSamples){
        for(int i = 0; i + numberOfSamples < MAX_RETENTION_COUNT; i++){
            collection[i] = collection[i + numberOfSamples];
        }
        nextWritePosition = Math.max(0, nextWritePosition - numberOfSamples);
    }

    public Sample[] getSnapshot(){
        // The snapshot size may vary, since "empty" slots won't be returned.
        Sample[] result = new Sample[nextWritePosition];
        for(int i = 0; i < nextWritePosition; i++){
            result[i] = collection[i];
        }
        return result;
    }

    // Required so the class can be tested properly even though it's a Singleton.
    public static void reset(){
        mInstance = null;
    }

}
