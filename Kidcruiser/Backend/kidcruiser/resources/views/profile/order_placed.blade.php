@extends('layouts.app')

<script>
    setTimeout(function () {
       window.location.href = "/plan"; //will redirect 
    }, 3000); //will call the function after 3 secs.
</script>

@section('content')

<div>
    <h2>Please wait ... </h2>
</div>
@endsection
