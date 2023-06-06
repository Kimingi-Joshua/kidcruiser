@extends('layouts.app')

@section('content')
    <?php
    $school = auth()->user();
    $current_children = 0;;
    for ($x = 0; $x < $school->parents->count(); $x++) {
        for ($y = 0; $y < $school->parents[$x]->children->count(); $y++) {
            $current_children++;
        }
    }
    $max = auth()->user()->plan->is_pay_as_you_go == 1? 100000 : auth()->user()->plan->allowed_children-$current_children;
    ?>
    <parents-edit v-bind:maximum="{{$max}}"></parents-edit>
@endsection
