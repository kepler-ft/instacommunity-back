CREATE TABLE communities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    contact VARCHAR(200) NOT NULL,
    contact2 VARCHAR(200) DEFAULT '',
    contact3 VARCHAR(200) DEFAULT ''
);

CREATE TABLE users_communities (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    community_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (community_id) REFERENCES communities(id),
    UNIQUE (user_id, community_id)
);
