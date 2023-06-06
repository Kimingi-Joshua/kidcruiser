@extends('layouts.auth')

@section('auth')
<div class="col-md-6">
    <div class="card mx-4">
        <div class="card-body p-4">
            <h3>{{ __('Register Your School') }}</h3>
            <p class="text-muted">Create your school account</p>

            <form method="POST" action="{{ route('register') }}">
                @csrf
                <div class="input-group mb-3">
                    <div class="input-group-prepend">
                        <span class="input-group-text">
                            <i class="nav-icon fas fa-landmark"></i>
                        </span>
                    </div>
                    <input id="name" type="text" 
                    class="form-control{{ $errors->has('name') ? ' is-invalid' : '' }}" 
                    name="name" value="{{ old('name') }}"  
                    placeholder="{{ __('School Name') }}" required autofocus>

                    @if ($errors->has('name'))
                        <span class="invalid-feedback" role="alert">
                            <strong>{{ $errors->first('name') }}</strong>
                        </span>
                    @endif
                </div>
                <div class="input-group mb-3">
                    <div class="input-group-prepend">
                        <span class="input-group-text">
                            <i class="fas fa-envelope-square"></i>
                        </span>
                    </div>
                    <input id="email" type="email" 
                    class="form-control{{ $errors->has('email') ? ' is-invalid' : '' }}" 
                    name="email" value="{{ old('email') }}" 
                    placeholder="{{ __('Admin Email Address') }}" required>

                    @if ($errors->has('email'))
                        <span class="invalid-feedback" role="alert">
                            <strong>{{ $errors->first('email') }}</strong>
                        </span>
                    @endif
                </div>
                <div class="input-group mb-3">
                    <div class="input-group-prepend">
                        <span class="input-group-text">
                            <i class="fas fa-phone"></i>
                        </span>
                    </div>
                    <div class="input-group-prepend">
                        <span class="input-group-text">
                            +
                        </span>
                    </div>
                    <input id="country_code" type="text"
                        class="form-control{{ $errors->has('country_code') ? ' is-invalid' : '' }}" 
                        name="country_code" value="{{ old('country_code') }}" 
                        placeholder="{{ __('Country Code') }}" required>
                    <input id="tel_number" type="text"
                        class="form-control{{ $errors->has('tel_number') ? ' is-invalid' : '' }}" 
                        name="tel_number" value="{{ old('tel_number') }}" 
                        placeholder="{{ __('Telephone Number') }}" required>

                        @if ($errors->has('country_code'))
                            <span class="invalid-feedback" role="alert">
                                <strong>{{ $errors->first('country_code') }}</strong>
                            </span>
                        @endif
                        
                        @if ($errors->has('tel_number'))
                            <span class="invalid-feedback" role="alert">
                                <strong>{{ $errors->first('tel_number') }}</strong>
                            </span>
                        @endif

                </div>
                <div class="input-group mb-3">
                    <div class="input-group-prepend">
                        <span class="input-group-text">
                            <i class="fas fa-lock"></i>
                        </span>
                    </div>
                    <input id="password" type="password" 
                    class="form-control{{ $errors->has('password') ? ' is-invalid' : '' }}"  
                    placeholder="{{ __('Password') }}" name="password" required>

                    @if ($errors->has('password'))
                        <span class="invalid-feedback" role="alert">
                            <strong>{{ $errors->first('password') }}</strong>
                        </span>
                    @endif
                </div>
                <div class="input-group mb-4">
                    <div class="input-group-prepend">
                        <span class="input-group-text">
                            <i class="fas fa-lock"></i>
                        </span>
                    </div>
                    <input id="password-confirm" type="password" class="form-control" name="password_confirmation" placeholder="{{ __('Confirm Password') }}" required>
                </div>
                <div class="input-group mb-3">
                    {!! htmlFormSnippet() !!}
                </div>
                <button type="submit" class="btn btn-block btn-success btn-primary">
                    {{ __('Create Account') }}
                </button>
            </form>
        </div>
        <div class="card-footer p-4">
            <div class="row">
                <div class="col-12">
                    <a class="btn btn-outline-primary btn-block" href="{{ route('login') }}">{{ __('Login') }}</a>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
