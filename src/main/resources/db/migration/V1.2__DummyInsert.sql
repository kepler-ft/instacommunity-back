-- INSERT INTO tabela(campo1, campo2) VALUES ('valor1', 2)
INSERT INTO users(name)
VALUES ('Ada');

INSERT INTO communities(name, contact, description)
VALUES ('Getting Started', 'https://github.com/kepler-ft',
        'Kepler-42, anteriormente conhecido como KOI-961, ' ||
        'é uma estrela anã vermelha localizada na constelação de ' ||
        'Cygnus a cerca de 126 anos-luz a partir do Sol. ' ||
        'Ela tem três planetas extrassolares conhecidos, ' ||
        'os quais tem raios menores do que o raio da Terra,[1] e, ' ||
        'provavelmente, também são menores em massa.');

INSERT INTO users_communities(user_id, community_id)
VALUES (1, 1);