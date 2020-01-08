# --------------------------------------------------------
# Host:                         localhost
# Database:                     slavek_bd2
# Server version:               5.6.46
# Server OS:                    Win32
# HeidiSQL version:             5.0.0.3031
# Date/time:                    2020-01-08 00:02:36
# --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
# Dumping data for table slavek_bd2.kierownik: 5 rows
/*!40000 ALTER TABLE `kierownik` DISABLE KEYS */;
INSERT INTO `kierownik` (`id`, `nazwisko`, `imie`, `login`, `haslo`, `zwolniony`) VALUES (1, 'Root', 'Root', 'roooot1234', '017e66004438b2790e6320bb5c12b95f', 0), (2, 'Kabulak', 'Karol', 'karkab8074', '16d7a4fca7442dda3ad93c9a726597e4', 0), (3, 'Wójcik', 'Klaudia', 'klawoj8275', '16d7a4fca7442dda3ad93c9a726597e4', 0), (4, 'Kała', 'Dominik', 'domkal2425', '16d7a4fca7442dda3ad93c9a726597e4', 0), (5, 'Szlauer', 'Szymon', 'szyszl1978', '16d7a4fca7442dda3ad93c9a726597e4', 0);
/*!40000 ALTER TABLE `kierownik` ENABLE KEYS */;

# Dumping data for table slavek_bd2.pracownicy: 12 rows
/*!40000 ALTER TABLE `pracownicy` DISABLE KEYS */;
INSERT INTO `pracownicy` (`id`, `nazwisko`, `imie`, `login`, `haslo`, `kierownik_id`, `zwolniony`) VALUES (1, 'Kabulak', 'Karol', 'karkab8074', '16d7a4fca7442dda3ad93c9a726597e4', 1, 1), (2, 'Wójcik', 'Klaudia', 'klawoj8275', '16d7a4fca7442dda3ad93c9a726597e4', 1, 1), (3, 'Kała', 'Dominik', 'domkal2425', '16d7a4fca7442dda3ad93c9a726597e4', 1, 1), (4, 'Szlauer', 'Szymon', 'szyszl1978', '16d7a4fca7442dda3ad93c9a726597e4', 1, 1), (5, 'Kowalski', 'Jan', 'jankow9399', '16d7a4fca7442dda3ad93c9a726597e4', 2, 0), (6, 'Rodowicz', 'Maryla', 'marrod9039', '16d7a4fca7442dda3ad93c9a726597e4', 2, 0), (7, 'Żuk', 'Andrzej', 'andzuk0253', '16d7a4fca7442dda3ad93c9a726597e4', 3, 0), (8, 'Panek', 'Maria', 'marpan6740', '16d7a4fca7442dda3ad93c9a726597e4', 3, 0), (9, 'Dąb-Kowalska', 'Aleksandra', 'aledab4030', '16d7a4fca7442dda3ad93c9a726597e4', 4, 0), (10, 'Rudnicka', 'Jessica', 'jesrud5975', '16d7a4fca7442dda3ad93c9a726597e4', 4, 0), (11, 'Kolanko', 'Mariusz', 'markol4379', '16d7a4fca7442dda3ad93c9a726597e4', 5, 0), (12, 'Nowak', 'Artur', 'artnow6846', '16d7a4fca7442dda3ad93c9a726597e4', 5, 0);
/*!40000 ALTER TABLE `pracownicy` ENABLE KEYS */;

# Dumping data for table slavek_bd2.slownik_cennik: 4 rows
/*!40000 ALTER TABLE `slownik_cennik` DISABLE KEYS */;
INSERT INTO `slownik_cennik` (`id`, `nazwa`) VALUES (1, 'Szkolny'), (2, 'Student'), (3, 'Normalny'), (4, 'Senior');
/*!40000 ALTER TABLE `slownik_cennik` ENABLE KEYS */;

# Dumping data for table slavek_bd2.cennik: 4 rows
/*!40000 ALTER TABLE `cennik` DISABLE KEYS */;
INSERT INTO `cennik` (`id`, `od`, `kierownik_id`, `odw_przed_wej`) VALUES (1, '2019-12-01 00:00:00', 2, 0), (2, '2020-03-01 00:00:00', 3, 0), (3, '2020-01-01 00:00:00', 4, 0), (4, '2020-02-01 00:00:00', 5, 0);
/*!40000 ALTER TABLE `cennik` ENABLE KEYS */;

# Dumping data for table slavek_bd2.poz_cennik: 14 rows
/*!40000 ALTER TABLE `poz_cennik` DISABLE KEYS */;
INSERT INTO `poz_cennik` (`id`, `cena`, `cennik_id`, `slownik_cennik_id`) VALUES (1, 0.6, 1, 1), (2, 0.5, 1, 2), (3, 0.85, 1, 3), (4, 0.45, 1, 4), (5, 0.5, 2, 1), (6, 0.5, 2, 2), (7, 0.8, 2, 3), (8, 0.5, 2, 4), (9, 0.55, 3, 1), (10, 0.45, 3, 2), (11, 0.8, 3, 3), (12, 0.45, 3, 4), (13, 0.65, 4, 1), (14, 0.55, 4, 2), (15, 0.9, 4, 3), (16, 0.45, 4, 4);
/*!40000 ALTER TABLE `poz_cennik` ENABLE KEYS */;

# Dumping data for table slavek_bd2.wyciag: 6 rows
/*!40000 ALTER TABLE `wyciag` DISABLE KEYS */;
INSERT INTO `wyciag` (`id`, `nazwa`, `wysokosc`) VALUES (1, 'Kocioł gorczykowy', 927), (2, 'Korbiełów', 757), (3, 'Skrzyczne', 704), (4, 'Szrenica', 587), (5, 'Jaworzynka', 373), (6, 'Małe skrzyczne', 586);
/*!40000 ALTER TABLE `wyciag` ENABLE KEYS */;

# Dumping data for table slavek_bd2.wyciag_dane: 6 rows
/*!40000 ALTER TABLE `wyciag_dane` DISABLE KEYS */;
INSERT INTO `wyciag_dane` (`id`, `od`, `koszt_pkt`, `stan`, `nieistniejacy`, `wyciag_id`, `kierownik_id`) VALUES (1, '2019-12-01 13:16:43', 40, 1, 0, 1, 2), (2, '2019-12-01 13:17:23', 35, 1, 0, 2, 2), (3, '2019-12-01 13:17:41', 30, 1, 0, 3, 2), (4, '2019-12-01 13:18:07', 25, 1, 0, 4, 2), (5, '2019-12-01 13:18:37', 15, 1, 0, 5, 2), (6, '2019-12-01 13:21:21', 30, 1, 0, 6, 2);
/*!40000 ALTER TABLE `wyciag_dane` ENABLE KEYS */;

# Dumping data for table slavek_bd2.zarzadcy: 11 rows
/*!40000 ALTER TABLE `zarzadcy` DISABLE KEYS */;
INSERT INTO `zarzadcy` (`id`, `od`, `do`, `kierownik_id`, `wyciag_id`) VALUES (1, '2019-12-01 13:16:43', NULL, 2, 1), (2, '2019-12-01 13:17:23', NULL, 2, 2), (3, '2019-12-01 13:17:41', NULL, 2, 3), (4, '2019-12-01 13:18:07', NULL, 2, 4), (5, '2019-12-01 13:18:37', NULL, 2, 5), (6, '2019-12-01 13:20:08', NULL, 3, 3), (7, '2019-12-01 13:20:18', NULL, 4, 4), (8, '2019-12-01 13:20:28', NULL, 5, 5), (9, '2019-12-01 13:20:56', NULL, 3, 2), (10, '2019-12-01 13:21:21', NULL, 2, 6), (11, '2019-12-01 13:21:30', NULL, 4, 6);
/*!40000 ALTER TABLE `zarzadcy` ENABLE KEYS */;

# Dumping data for table slavek_bd2.karnet: 2 rows
/*!40000 ALTER TABLE `karnet` DISABLE KEYS */;
INSERT INTO `karnet` (`id`, `zablokowany`) VALUES (1, 0), (2, 0), (3, 0);
/*!40000 ALTER TABLE `karnet` ENABLE KEYS */;

# Dumping data for table slavek_bd2.hist_dolad: 2 rows
/*!40000 ALTER TABLE `hist_dolad` DISABLE KEYS */;
INSERT INTO `hist_dolad` (`id`, `l_pkt`, `stempelczasowy`, `karnet_id`, `pracownicy_id`, `poz_cennik_id`) VALUES (1, 100, '2019-12-01 13:32:03', 1, 5, 1), (2, 200, '2019-12-01 13:32:27', 2, 5, 2), (3, 500, '2019-12-01 13:32:40', 3, 5, 4), (4, 200, '2019-12-01 16:25:12', 1, 5, 1), (5, 120, '2019-12-21 08:22:57', 2, 5, 3);
/*!40000 ALTER TABLE `hist_dolad` ENABLE KEYS */;

# Dumping data for table slavek_bd2.uzycia_karnetu: 24 rows
/*!40000 ALTER TABLE `uzycia_karnetu` DISABLE KEYS */;
INSERT INTO `uzycia_karnetu` (`id`, `stempelczasowy`, `wyciag_dane_id`, `karnet_id`) VALUES (1, '2019-12-01 13:34:07', 1, 1), (2, '2019-12-01 13:34:32', 2, 2), (3, '2019-12-01 13:34:38', 6, 3), (4, '2019-12-01 15:24:07', 1, 1), (5, '2019-12-01 15:24:13', 5, 2), (6, '2019-12-01 15:24:20', 6, 3), (7, '2019-12-01 16:24:48', 5, 1), (8, '2019-12-01 16:24:57', 1, 3), (9, '2019-12-05 10:14:32', 5, 1), (10, '2019-12-05 10:14:37', 1, 2), (11, '2019-12-05 13:14:51', 6, 3), (12, '2019-12-05 13:15:00', 4, 1), (13, '2019-12-21 08:22:07', 1, 3), (14, '2019-12-21 08:22:19', 3, 3), (15, '2019-12-21 08:22:28', 3, 2), (16, '2020-01-03 14:11:41', 4, 1), (17, '2020-01-03 14:11:45', 6, 3), (18, '2020-01-03 14:11:50', 2, 2), (19, '2020-01-03 16:20:59', 6, 1), (20, '2020-01-03 16:21:04', 6, 3), (21, '2020-01-06 10:11:18', 3, 1), (22, '2020-01-06 10:11:25', 2, 3), (23, '2020-01-06 14:11:34', 5, 1), (24, '2020-01-06 14:11:38', 2, 2), (25, '2020-01-06 16:32:46', 6, 3), (26, '2020-01-06 16:32:50', 2, 1);
/*!40000 ALTER TABLE `uzycia_karnetu` ENABLE KEYS */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
