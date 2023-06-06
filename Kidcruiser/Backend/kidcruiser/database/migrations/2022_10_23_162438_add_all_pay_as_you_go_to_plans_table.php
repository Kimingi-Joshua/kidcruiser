<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class AddAllPayAsYouGoToPlansTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::table('plans', function (Blueprint $table) {
            //
            $table->boolean('is_pay_as_you_go')->default(false);
            $table->boolean('is_free')->default(false);
            $table->integer('allowed_drivers')->default(0)->change();
            $table->integer('allowed_children')->default(0)->change();
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::table('plans', function (Blueprint $table) {
            //
            $table->dropColumn('is_pay_as_you_go');
        });
    }
}
