<?php

namespace App\Http\Controllers\Auth;

use App\User;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Foundation\Auth\RegistersUsers;
use Illuminate\Support\Facades\Storage;

use Avatar;
use App\Setting;
use App\SettingType;
use App\Http\Traits\PaymentHandle;

class RegisterController extends Controller
{
    use PaymentHandle;
    /*
    |--------------------------------------------------------------------------
    | Register Controller
    |--------------------------------------------------------------------------
    |
    | This controller handles the registration of new schools as well as their
    | validation and creation. By default this controller uses a trait to
    | provide this functionality without requiring any additional code.
    |
    */

    use RegistersUsers;

    /**
     * Where to redirect users after registration.
     *
     * @var string
     */
    protected $redirectTo = '/home';

    /**
     * Create a new controller instance.
     *
     * @return void
     */
    public function __construct()
    {
        $this->middleware('guest');
    }

    /**
     * Get a validator for an incoming registration request.
     *
     * @param  array  $data
     * @return \Illuminate\Contracts\Validation\Validator
     */
    protected function validator(array $data)
    {
        return Validator::make($data, [
            'name' => 'required|string|max:50|unique:schools',
            'email' => 'required|string|email|max:50|unique:schools',
            'country_code' => 'required|string|numeric|digits_between:1,4',
            'tel_number' => 'required|string|numeric|digits_between:1,15',
            'password' => 'required|string|min:6|confirmed',
            'g-recaptcha-response' => 'recaptcha',
        ]);
    }

    /**
     * Create a new school instance after a valid registration.
     *
     * @param  array  $data
     * @return \App\User
     */
    protected function create(array $data)
    {
        $school = User::create([
            'name' => $data['name'],
            'email' => $data['email'],
            'country_code' => $data['country_code'],
            'tel_number' => $data['tel_number'],
            'channel' => uniqid(),
            'password' => Hash::make($data['password']),
        ]);

        $avatar = Avatar::create($school->name)->getImageObject()->encode('png');
        Storage::put('avatars/'.$school->id.'/avatar.png', (string) $avatar);

        $this->createCustomerPaymentGateway($school);

        return $school;
    }
}
