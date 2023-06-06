<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class UpdatePaymentSchoolsTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        $keys = ['card_brand', 'card_last_four', 'trial_ends_at'];
        foreach ($keys as $key => $k) {
            if (Schema::hasColumn('schools', $k)) {
                Schema::table('schools',  function ($table) use ($k) {
                    $table->dropColumn($k);
                });
            }
        }
        Schema::table('schools', function (Blueprint $table) {
            //
            $table->string('razorpay_id')->nullable()->index();
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {

    }
}