-- First, drop the default constraint on the column.
ALTER TABLE m_users ALTER COLUMN f_two_factor_verified DROP DEFAULT;

-- Now, alter the column type, converting existing integer values to boolean.
ALTER TABLE m_users ALTER COLUMN f_two_factor_verified TYPE BOOLEAN USING (f_two_factor_verified != 0);

-- Finally, set a new boolean default value.
ALTER TABLE m_users ALTER COLUMN f_two_factor_verified SET DEFAULT false;