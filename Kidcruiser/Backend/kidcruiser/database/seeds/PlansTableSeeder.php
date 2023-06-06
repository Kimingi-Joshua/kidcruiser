<?php

use Illuminate\Database\Seeder;

class PlansTableSeeder extends Seeder
{
    /**
     * Run the database seeds.
     *
     * @return void
     */
    public function run()
    {
        DB::table('plans')->insert([
            [
                'name' => 'Free',
                'price' => 0,
                'allowed_drivers' => 3,
                'allowed_children' =>5,
                'is_free' => 1,
            ],
        ]);

        DB::table('plans')->insert([
            [
                'name' => 'Pay As You Go',
                'price' => 10,
                'is_pay_as_you_go' => 1
            ],
        ]);

        DB::table('custom_plan_settings')->insert([
            [
                'price_per_seat' => 1,
                'price_per_driver' => 1
            ],
        ]);
    }
}
