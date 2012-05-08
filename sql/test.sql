
DROP TABLE IF EXISTS company;
CREATE TABLE company (
           id    INT PRIMARY KEY AUTO_INCREMENT,
           name  VARCHAR(30),
		   creationdate DATE
);

DROP TABLE IF EXISTS subsidiary;
CREATE TABLE subsidiary (
			id INT PRIMARY KEY AUTO_INCREMENT,
			name VARCHAR(30),
			workforce INT,
			companyId INT
);
