INSERT INTO kierownik(nazwisko, imie, login, haslo) VALUES (
	'Root',
	'Root',
	'roooot1234',
	MD5('roooot1234')
);

INSERT INTO slownik_cennik(nazwa) VALUES ('Szkolny');
INSERT INTO slownik_cennik(nazwa) VALUES ('Student');
INSERT INTO slownik_cennik(nazwa) VALUES ('Nornalny');
INSERT INTO slownik_cennik(nazwa) VALUES ('Senior');