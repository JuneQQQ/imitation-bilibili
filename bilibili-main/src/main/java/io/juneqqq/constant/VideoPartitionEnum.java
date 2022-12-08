package io.juneqqq.constant;

public enum VideoPartitionEnum {
    // 0其他 1音乐 2电影 3游戏 4鬼畜 5番剧
    OTHER(0,"其他"),MUSIC(1,"音乐"),MOVIE(2,"电影"),
    GAME(3,"游戏"),AUTOTUNEREMIX(4,"鬼畜"),ANIME(5,"番剧");

    public final int code;
    public final String name;
    VideoPartitionEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
