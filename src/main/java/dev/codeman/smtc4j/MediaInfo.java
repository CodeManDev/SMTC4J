package dev.codeman.smtc4j;

import java.util.Base64;

public record MediaInfo(String title, String artist, String album, double duration, String sourceApp,
                        String thumbnailBase64) {
    @Override
    public String toString() {
        return "MediaInfo{" +
                "title='" + this.title + '\'' +
                ", artist='" + this.artist + '\'' +
                ", album='" + this.album + '\'' +
                ", duration=" + this.duration +
                ", sourceApp='" + this.sourceApp + '\'' +
                ", thumbnailBase64='" + this.thumbnailBase64 + '\'' +
                '}';
    }

    public byte[] getThumbnailBytes() {
        if (this.thumbnailBase64 == null || this.thumbnailBase64.isEmpty()) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(this.thumbnailBase64);
    }
}