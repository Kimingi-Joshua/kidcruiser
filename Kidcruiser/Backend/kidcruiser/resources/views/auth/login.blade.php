@extends('layouts.auth')

@section('auth')
<?php 
$system_name = App\Setting::where('name', 'System name')->first()->value;
?>
<div class="col-md-8">
    <div class="card-group">
        <div class="card">
            <div class="card-body p-5">
                <a class="text-center d-lg-none" href="{{ url('/') }}">
                    {{-- <img src="svg/modulr.svg" class="mb-5" width="150" alt="Modulr Logo"> --}}
                    <h2 class="logo-text yellow pb-5">{{$system_name}}</h2>
                </a>
                <h3>{{ __('Login') }}</h3>
                <p class="text-muted">Sign In to your school account</p>

                <form method="POST" action="{{ route('login') }}">
                    @csrf
                    <div class="input-group mb-3">
                        <div class="input-group-prepend">
                            <span class="input-group-text">
                                <i class="fas fa-envelope-square"></i>
                            </span>
                        </div>
                        <input id="email" type="email" 
                        class="form-control{{ $errors->has('email') ? ' is-invalid' : '' }}" 
                        name="email" value="{{ old('email') }}" 
                        placeholder="{{ __('Admin Email Address') }}" required autofocus>

                        @if ($errors->has('email'))
                        <span class="invalid-feedback" role="alert">
                            <strong>{{ $errors->first('email') }}</strong>
                        </span>
                        @endif
                    </div>
                    <div class="input-group mb-3">
                        <div class="input-group-prepend">
                            <span class="input-group-text">
                                <i class="fas fa-lock"></i>
                            </span>
                        </div>
                        <input id="password" type="password" class="form-control{{ $errors->has('password') ? ' is-invalid' : '' }}" name="password" placeholder="{{ __('Password') }}" required>

                        @if ($errors->has('password'))
                        <span class="invalid-feedback" role="alert">
                            <strong>{{ $errors->first('password') }}</strong>
                        </span>
                        @endif
                    </div>
                    <div class="input-group mb-3">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" name="remember" id="remember" {{ old('remember') ? 'checked' : '' }}>
                        </div>
                        <label class="form-check-label" for="remember">
                            {{ __('Remember Me') }}
                        </label>
                    </div>
                    <div class="row">
                        <div class="col-4">
                            <button type="submit" class="btn btn-primary px4">
                                {{ __('Login') }}
                            </button>
                        </div>
                        <div class="col-8 text-right">
                            <a class="btn btn-link px-0" href="{{ route('password.request') }}">
                                {{ __('Forgot Your Password?') }}
                            </a>
                        </div>
                    </div>
                </form>
            </div>
            <div class="card-footer p-4 d-lg-none">
                <div class="col-12 text-right">
                    <a class="btn btn-outline-primary btn-block mt-3" href="{{ route('register') }}">{{ __('Register') }}</a>
                </div>
            </div>
        </div>
        <div class="card text-white header d-md-down-none">
            <div class="card-body text-center">
                <div>
                    <a href="{{ url('/') }}">
                        <h2 class="logo-text yellow py-5">{{$system_name}}</h2>
                    </a>
                    {{-- <img src="svg/modulr.svg" class="mb-5" width="150" alt="Modulr Logo"> --}}
                    <h4>{{ __('Sign up') }}</h4>
                    <p>If you don't have account create one.</p>
                    <a class="btn btn-primary mt-2" href="{{ route('register') }}">{{ __('Register Now!') }}</a>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
