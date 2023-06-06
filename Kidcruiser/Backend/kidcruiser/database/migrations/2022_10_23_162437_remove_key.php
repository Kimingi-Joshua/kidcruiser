<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class RemoveKey extends Migration
{
    public function up()
    {
        $keys = ['v_code', 'secretKey'];
        foreach ($keys as $key => $k) {
            if (Schema::hasColumn('parents', $k)) {
                Schema::table('parents',  function ($table) use ($k) {
                    $table->dropColumn($k);
                });
            }
            if (Schema::hasColumn('drivers', $k)) {
                Schema::table('drivers',  function ($table) use ($k) {
                    $table->dropColumn($k);
                });
            }
        }
    }

    public function down()
    {
        $keys = ['v_code', 'secretKey'];
        foreach ($keys as $key => $k) {
            Schema::table('parents', function ($table) use ($k) {
                $table->string($k);
            });
            Schema::table('drivers', function ($table) use ($k) {
                $table->string($k);
            });
        }
    }
}
