# Java Concurrency, Synchronization, and Testing

## Overview
This repository contains project focusing on Java concurrency, synchronization, and unit testing. The project involves implementing a simplified version of the "Set" card game.

## Table of Contents
1. [Introduction](#introduction)
2. [Game Design](#game-design)
3. [Getting Started](#getting-started)
4. [Project Structure](#project-structure)
5. [Building and Running](#building-and-running)

## Introduction
The goal of this project is to practice concurrent programming in a Java 8 environment and gain experience with unit testing. 
It focuses on Java threads, synchronization, and the implementation of a simplified "Set" card game.

## Game Design
The game involves a deck of 81 cards with different features. Players aim to find a legal set of three cards based on specific rules. 
The game includes human and non-human players, threading, and a graphical user interface.

## Getting Started
The assignment requires Maven as the build tool.

## Project Structure
- `src/main/java/bguspl/set/ex`: Contains the main implementation of the assignment.
- `src/test`: Includes unit tests for Table, Dealer, and Player classes.

## Building and Running
To compile and run the project, use the following Maven commands:
```bash
mvn clean compile test
java -cp target/classes bguspl.set.Main
