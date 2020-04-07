package pl.edu.ug;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static final String KEY = "key.txt";
    public static final String PLAIN = "plain.bmp";
    public static final String ECB_FILE = "ecb_crypto.bmp";
    public static final String CBC_FILE = "cbc_crypto.bmp";
    private static final int DELTA = 0x9e3779b9;

    public static void main(String[] args) throws IOException {
        ECB();
        CBC();
    }

    public static int[] keyReader() throws FileNotFoundException {
        int[] key = new int[4];
        File file = new File(KEY);
        if (file.exists()) {
            Scanner scanner = new Scanner(file);
            String keyString = scanner.nextLine();
            if (keyString.length() != 4) {
                System.out.println("Klucz musi mieć długość 4");
                System.exit(0);
            }
            for (int i = 0; i < 4; i++) {
                key[i] = keyString.charAt(i);
            }
        } else {
            key[0] = 10;
            key[1] = 20;
            key[2] = 30;
            key[3] = 40;
        }
        return key;
    }

    public static int[] ECBEncrypt(int[] plainText, int[] key) {
        int sum = 0;
        int left = plainText[0];
        int right = plainText[1];

        for (int i = 0; i < 32; i++) {
            sum += DELTA;
            left += ((right << 4) + key[0]) ^ (right + sum) ^ ((right >> 5) + key[1]);
            right += ((left << 4) + key[2]) ^ (left + sum) ^ ((left >> 5) + key[3]);
        }

        int[] block = new int[2];
        block[0] = left;
        block[1] = right;

        return block;
    }

    public static void ECB() throws IOException {
        int[] img = new int[2];
        int[] key = keyReader();

        FileInputStream imgIn = new FileInputStream(PLAIN);
        FileOutputStream imgOut = new FileOutputStream(ECB_FILE);
        DataInputStream dataIn = new DataInputStream(imgIn);
        DataOutputStream dataOut = new DataOutputStream(imgOut);

        for (int i = 0; i < 10; i++) {
            if (dataIn.available() > 0) {
                img[0] = dataIn.readInt();
                img[1] = dataIn.readInt();
                dataOut.writeInt(img[0]);
                dataOut.writeInt(img[1]);
            }
        }

        int[] cipher;
        boolean check = true;

        while (dataIn.available() > 0) {
            try {
                img[0] = dataIn.readInt();
                check = true;
                img[1] = dataIn.readInt();
                cipher = ECBEncrypt(img, key);
                dataOut.writeInt(cipher[0]);
                dataOut.writeInt(cipher[1]);
                check = false;
            } catch (EOFException e) {
                dataOut.writeInt(img[0]);
                if (!check) {
                    dataOut.writeInt(img[1]);
                }
            }
        }
        dataIn.close();
        dataOut.close();
    }

    public static int[] CBCEncrypt(int[] plainText, int[] previous, int[] key) {
        int sum = 0;
        int left = plainText[0] ^ previous[0];
        int right = plainText[1] ^ previous[1];

        for (int i = 0; i < 32; i++) {
            sum += DELTA;
            left += ((right << 4) + key[0]) ^ (right + sum) ^ ((right >> 5) + key[1]);
            right += ((left << 4) + key[2]) ^ (left + sum) ^ ((left >> 5) + key[3]);
        }
        int[] block = new int[2];
        block[0] = left;
        block[1] = right;
        return block;
    }

    public static void CBC() throws IOException {
        int[] img = new int[2];
        int[] key = keyReader();
        Random rand = new Random();
        int[] random = {rand.nextInt(), rand.nextInt()};

        FileInputStream imgIn = new FileInputStream(PLAIN);
        FileOutputStream imgOut = new FileOutputStream(CBC_FILE);
        DataInputStream dataIn = new DataInputStream(imgIn);
        DataOutputStream dataOut = new DataOutputStream(imgOut);

        for (int i = 0; i < 10; i++) {
            if (dataIn.available() > 0) {
                img[0] = dataIn.readInt();
                img[1] = dataIn.readInt();
                dataOut.writeInt(img[0]);
                dataOut.writeInt(img[1]);
            }
        }
        boolean firstTime = true;
        int[] cipher = new int[2];
        boolean check = true;
        while (dataIn.available() > 0) {
            try {
                img[0] = dataIn.readInt();
                check = true;
                img[1] = dataIn.readInt();
                if (firstTime) {
                    cipher = CBCEncrypt(img, random, key);
                    firstTime = false;
                } else {
                    cipher = CBCEncrypt(img, cipher, key);
                }

                dataOut.writeInt(cipher[0]);
                dataOut.writeInt(cipher[1]);
                check = false;
            } catch (EOFException e) {
                dataOut.writeInt(img[0]);
                if (!check) {
                    dataOut.writeInt(img[1]);
                }
            }
        }
        dataIn.close();
        dataOut.close();
    }
}
