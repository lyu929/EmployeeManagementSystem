-- Divisions and job titles of Company Z (from the course data set).

INSERT INTO division (id, name, city, address_line1, address_line2, state, country, postal_code) VALUES
    (1,   'Technology Engineering', 'Atlanta',  '200 17th Street NW',  NULL, 'GA', 'USA', '30363'),
    (2,   'Marketing',              'Atlanta',  '200 17th Street NW',  NULL, 'GA', 'USA', '30363'),
    (3,   'Human Resources',        'New York', '45 West 57th Street', NULL, 'NY', 'USA', '00034'),
    (999, 'HQ',                     'New York', '45 West 57th Street', NULL, 'NY', 'USA', '00034');

INSERT INTO job_title (id, title) VALUES
    (100, 'Software Manager'),
    (101, 'Software Architect'),
    (102, 'Software Engineer'),
    (103, 'Software Developer'),
    (200, 'Marketing Manager'),
    (201, 'Marketing Associate'),
    (202, 'Marketing Assistant'),
    (900, 'Chief Executive Officer'),
    (901, 'Chief Financial Officer'),
    (902, 'Chief Information Officer');
