    DROP DATABASE IF EXISTS painting;
    CREATE DATABASE painting;
    USE painting;

    CREATE TABLE Users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user VARCHAR(255) NOT NULL,
        password VARCHAR(255) NOT NULL
    );

    /* consultar usuarios*/

    select * from Users;



