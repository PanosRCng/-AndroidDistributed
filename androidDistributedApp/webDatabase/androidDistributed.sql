-- phpMyAdmin SQL Dump
-- version 3.5.8
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 25, 2013 at 10:11 PM
-- Server version: 5.5.30-MariaDB-log
-- PHP Version: 5.3.25-pl0-gentoo

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `androidDistributed`
--
CREATE DATABASE `androidDistributed` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `androidDistributed`;

-- --------------------------------------------------------

--
-- Table structure for table `experiments`
--

CREATE TABLE IF NOT EXISTS `experiments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `contextType` varchar(256) NOT NULL,
  `user_email` varchar(256) NOT NULL,
  `name` varchar(256) NOT NULL,
  `sensorDependencies` text NOT NULL,
  `timeDependencies` text NOT NULL,
  `expires` varchar(256) NOT NULL,
  `status` varchar(256) NOT NULL,
  `url` varchar(256) NOT NULL,
  `executedBy` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=19 ;

--
-- Dumping data for table `experiments`
--

INSERT INTO `experiments` (`id`, `contextType`, `user_email`, `name`, `sensorDependencies`, `timeDependencies`, `expires`, `status`, `url`, `executedBy`) VALUES
(18, 'org.ambientdynamix.contextplugins.addplugin', 'user1@gmail.com', 'Add plugin', '|gpsPosition|', '1,2,3', 'never', '', 'http://150.140.210.98/androidDistributed/dataPlace/user1/org.ambientdynamix.contextplugins.addplugin.jar', 0);

-- --------------------------------------------------------

--
-- Table structure for table `plugins`
--

CREATE TABLE IF NOT EXISTS `plugins` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `plugin_id` varchar(256) NOT NULL,
  `runtimeFactoryClass` varchar(256) NOT NULL,
  `name` varchar(256) NOT NULL,
  `description` varchar(256) NOT NULL,
  `installUrl` varchar(256) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `plugins`
--

INSERT INTO `plugins` (`id`, `plugin_id`, `runtimeFactoryClass`, `name`, `description`, `installUrl`) VALUES
(1, 'org.ambientdynamix.contextplugins.addplugin', 'org.ambientdynamix.contextplugins.addplugin.PluginFactory', 'Add Plugin', 'Add Plugin', '/dynamix/org.ambientdynamix.contextplugins.addplugin_9.47.1.jar');

-- --------------------------------------------------------

--
-- Table structure for table `results`
--

CREATE TABLE IF NOT EXISTS `results` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `experiment_id` int(11) NOT NULL,
  `source_id` int(11) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `smartphones`
--

CREATE TABLE IF NOT EXISTS `smartphones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phone_id` int(11) NOT NULL,
  `sensors_rules` text NOT NULL,
  `time_rules` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=27 ;

--
-- Dumping data for table `smartphones`
--

INSERT INTO `smartphones` (`id`, `phone_id`, `sensors_rules`, `time_rules`) VALUES
(26, 1, '|gpsPosition|', 'time_rules');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `firstname` varchar(256) NOT NULL,
  `lastname` varchar(256) NOT NULL,
  `username` varchar(256) NOT NULL,
  `password` varchar(256) NOT NULL,
  `email` varchar(256) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=11 ;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `firstname`, `lastname`, `username`, `password`, `email`) VALUES
(7, 'user1', 'user1', 'user1', 'd0e4492c72e0941fbf1966a3310389f12562a4ca', 'user1@gmail.com'),
(8, 'user2', 'user2', 'user2', 'd0e4492c72e0941fbf1966a3310389f12562a4ca', 'user2@gmail.com'),
(9, 'user3', 'user3', 'user3', 'd0e4492c72e0941fbf1966a3310389f12562a4ca', 'user3@mail.com'),
(10, 'user4', 'user4', 'user4', 'd0e4492c72e0941fbf1966a3310389f12562a4ca', 'user4@gmail.com');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
