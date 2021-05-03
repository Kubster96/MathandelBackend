[![Build Status](https://travis-ci.org/Kubster96/MathandelBackend.svg?branch=master)](https://travis-ci.org/Kubster96/MathandelBackend)

# Mathandel Backend

Repository for Bachelor's of science thesis project

## Database setup

1. Download mySQL
2. mysql -u root -p
3. CREATE DATABASE dbMathandel;
4. CREATE DATABASE dbMathandelTest;
5. CREATE USER 'MathandelUser'@'localhost' IDENTIFIED BY 'MathandelRulez';
6. GRANT ALL ON dbMathandel.* TO 'MathandelUser'@'localhost';
7. GRANT ALL ON dbMathandelTest.* TO 'MathandelUser'@'localhost';
