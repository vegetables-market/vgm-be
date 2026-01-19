-- Add image, harvest season and address to t_items
ALTER TABLE t_items
    ADD COLUMN f_image_url varchar(500),
    ADD COLUMN f_harvest_season varchar(100),
    ADD COLUMN f_address varchar(500);

-- Optional: create uploads table mapping already exists (t_items_images) so leaving it.
