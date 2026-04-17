-- Fix: Set default value for examples_json column
-- This migration updates any NULL values to '[]' for backward compatibility
UPDATE cards SET examples_json = '[]' WHERE examples_json IS NULL OR examples_json = '';

