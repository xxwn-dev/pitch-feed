package com.xxwn.pitchfeed.global.util;

import java.util.Collection;

public final class SimHashUtil {

    private SimHashUtil() {}

    private static final int BITS = 64;
    static final int DUPLICATE_THRESHOLD = 10;

    /**
     * 제목 텍스트에서 64비트 SimHash fingerprint를 계산한다.
     * 한국어 띄어쓰기 불일치에 강하도록 문자 바이그램 기반으로 구현.
     *
     * @return fingerprint. 텍스트가 null/빈 값이면 0L 반환.
     */
    public static long compute(String text) {
        if (text == null || text.isBlank()) return 0L;
        String normalized = text.toLowerCase().replaceAll("\\s+", " ").trim();
        int[] bitVector = new int[BITS];

        for (int i = 0; i < normalized.length() - 1; i++) {
            String bigram = normalized.substring(i, i + 2);
            long h = fnv1a64(bigram);
            for (int b = 0; b < BITS; b++) {
                if (((h >>> b) & 1L) == 1L) {
                    bitVector[b]++;
                } else {
                    bitVector[b]--;
                }
            }
        }

        long fingerprint = 0L;
        for (int b = 0; b < BITS; b++) {
            if (bitVector[b] > 0) {
                fingerprint |= (1L << b);
            }
        }
        return fingerprint;
    }

    public static int hammingDistance(long a, long b) {
        return Long.bitCount(a ^ b);
    }

    /**
     * hash가 existingHashes 중 임계값 이하 Hamming distance인 항목이 있으면 중복으로 판정.
     * hash == 0L (빈 제목) 이면 항상 false.
     */
    public static boolean isDuplicateInSet(long hash, Collection<Long> existingHashes) {
        if (hash == 0L) return false;
        for (long existing : existingHashes) {
            if (hammingDistance(hash, existing) <= DUPLICATE_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private static long fnv1a64(String s) {
        long h = 0xcbf29ce484222325L; // FNV-1a 64bit offset basis
        for (char c : s.toCharArray()) {
            h ^= c;
            h *= 0x100000001b3L;     // FNV prime
        }
        return h;
    }
}
