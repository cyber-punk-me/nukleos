package me.cyber.nukleos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyoData {

    interface MyoDataListener{
        void onData(MyoResult readings);
    }

    public class MyoResult {
        private final byte[] data1;
        private final byte[] data2;

        MyoResult(byte[] data1, byte[] data2) {
            this.data1 = data1;
            this.data2 = data2;
        }

        public byte[] getData1() {
            return data1;
        }

        public byte[] getData2() {
            return data2;
        }

        @Override
        public String toString() {
            return Arrays.toString(data2);
        }
    }

    private List<MyoDataListener> listeners = new ArrayList<>();

    public static final MyoData MD = new MyoData();

    private MyoData() {
    }

    public void addListener(MyoDataListener l) {
        listeners.add(l);
    }

    //2 readings from from 8 sensors
    public MyoResult onInput(byte[] in) {
        byte[] data1 = new byte[8];
        System.arraycopy(in, 0, data1, 0, 8);

        byte[] data2 = new byte[8];
        System.arraycopy(in, 0, data2, 0, 8);

        MyoResult res = new MyoResult(data1, data2);

        for (MyoDataListener listener : listeners) {
            listener.onData(res);
        }

        return res;
    }

}
