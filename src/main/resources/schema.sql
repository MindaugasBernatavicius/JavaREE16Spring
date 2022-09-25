DROP TABLE IF EXISTS `blogpost`;

CREATE TABLE `blogpost` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `text` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
    `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;