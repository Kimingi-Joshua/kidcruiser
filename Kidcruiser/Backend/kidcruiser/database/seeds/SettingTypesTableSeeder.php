<?php

use Illuminate\Database\Seeder;

class SettingTypesTableSeeder extends Seeder
{
    /**
     * Run the database seeds.
     *
     * @return void
     */
    public function run()
    {
        //
        DB::table('setting_types')->insert([
            [
                'id' => 1,
                'name' => 'Google Maps',
                'enabled' => null,
            ],
        ]);
        DB::table('setting_types')->insert([
            [
                'id' => 2,
                'name' => 'Personalize your system',
                'enabled' => null,
            ],
        ]);
        DB::table('setting_types')->insert([
            [
                'id' => 3,
                'name' => 'Currency and billing cycle',
                'enabled' => null,
            ],
        ]);
        DB::table('setting_types')->insert([
            [
                'id' => 4,
                'name' => 'SMS',
                'enabled' => null,
            ],
        ]);
        DB::table('setting_types')->insert([
            [
                'id' => 5,
                'name' => 'Payment',
                'enabled' => null,
            ],
        ]);

        DB::table('setting_types')->insert([
            [
                'id' => 6,
                'name' => 'Show Ads in driver app',
            ],
        ]);

        DB::table('setting_types')->insert([
            [
                'id' => 7,
                'name' => 'Show Ads in parent app',
            ],
        ]);

        DB::table('setting_types')->insert([
            [
                'id' => 8,
                'name' => 'Send notifications as SMS',
            ],
        ]);

        DB::table('setting_types')->insert([
            [
                'id' => 9,
                'name' => 'Use MabBox in apps instead of Google Maps',
            ],
        ]);
    }
}
