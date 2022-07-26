package com.daba.sb;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    public static void main(String[] args) {
        log.info("Application is started");
        Game game = new Game();
        game.play();
        log.trace("Application is finished");
    }

}
