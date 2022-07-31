package com.daba.sb;

import com.daba.sb.process.Game;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    public static void main(String[] args) {
        log.info("Application is started");
        new Game().play();
        log.trace("Application is finished");
    }

}
