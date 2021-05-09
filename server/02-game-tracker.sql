CREATE
DATABASE  IF NOT EXISTS `game_server_db_tracker`;
USE
`game_server_db_tracker`;

--
-- Table structure for table `user_db`
--

DROP TABLE IF EXISTS `user_db`;

CREATE TABLE `user_db`
(
    `ttt_losses` int(11) DEFAULT 0,
    `ttt_wins`  int(11) DEFAULT 0,
    `ttt_diff`  int(11) DEFAULT 0,
    `ch_losses`  int(11) DEFAULT 0,
    `ch_wins`   int(11) DEFAULT 0,
    `ch_diff`   int(11) DEFAULT 0,
    `version`	int(11) DEFAULT 0,
    `user_name` varchar(45) NOT NULL,
    `password`  varchar(45) NOT NULL,
    PRIMARY KEY (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

