<!DOCTYPE html>
<html lang="{{ str_replace('_', '-', app()->getLocale()) }}">

<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <link rel="apple-touch-icon-precomposed" sizes="57x57" href="/icon/apple-touch-icon-57x57.png" />
  <link rel="apple-touch-icon-precomposed" sizes="114x114" href="/icon/apple-touch-icon-114x114.png" />
  <link rel="apple-touch-icon-precomposed" sizes="72x72" href="/icon/apple-touch-icon-72x72.png" />
  <link rel="apple-touch-icon-precomposed" sizes="144x144" href="/icon/apple-touch-icon-144x144.png" />
  <link rel="apple-touch-icon-precomposed" sizes="60x60" href="/icon/apple-touch-icon-60x60.png" />
  <link rel="apple-touch-icon-precomposed" sizes="120x120" href="/icon/apple-touch-icon-120x120.png" />
  <link rel="apple-touch-icon-precomposed" sizes="76x76" href="/icon/apple-touch-icon-76x76.png" />
  <link rel="apple-touch-icon-precomposed" sizes="152x152" href="/icon/apple-touch-icon-152x152.png" />
  <link rel="icon" type="image/png" href="/icon/favicon-196x196.png" sizes="196x196" />
  <link rel="icon" type="image/png" href="/icon/favicon-96x96.png" sizes="96x96" />
  <link rel="icon" type="image/png" href="/icon/favicon-32x32.png" sizes="32x32" />
  <link rel="icon" type="image/png" href="/icon/favicon-16x16.png" sizes="16x16" />
  <link rel="icon" type="image/png" href="/icon/favicon-128.png" sizes="128x128" />
  <meta name="msapplication-TileColor" content="#FFFFFF" />
  <meta name="msapplication-TileImage" content="/icon/mstile-144x144.png" />
  <meta name="msapplication-square70x70logo" content="/icon/mstile-70x70.png" />
  <meta name="msapplication-square150x150logo" content="/icon/mstile-150x150.png" />
  <meta name="msapplication-wide310x150logo" content="/icon/mstile-310x150.png" />
  <meta name="msapplication-square310x310logo" content="/icon/mstile-310x310.png" />

  <!-- CSRF Token -->
  <meta name="csrf-token" content="{{ csrf_token() }}">

  <title>{{ config('app.name', 'Laravel') }}</title>

  <!-- Scripts -->
  <script src="{{ asset('js/app.js') }}" defer></script>

  <script>
    window.Laravel = {
      !!json_encode([
        'csrfToken' => csrf_token(),
      ]) !!
    };
  </script>

  <script>
    if ('serviceWorker' in navigator) {
      window.addEventListener('load', function() {
        navigator.serviceWorker.register('/service-worker.js').then(function(registration) {
          // Registration was successful
          console.log('ServiceWorker registration successful with scope: ', registration.scope);
        }, function(err) {
          // registration failed :(
          console.log('ServiceWorker registration failed: ', err);
        });
      });
    }
  </script>
  <style>
    .error {
      color: red;
      font-weight: 400;
      display: block;
      padding: 6px 0;
      font-size: 14px;
    }

    .form-control.error {
      border-color: red;
      padding: .375rem .75rem;
    }
  </style>
  <!-- Fonts -->
  <link rel="dns-prefetch" href="https://fonts.gstatic.com">
  <link href="https://fonts.googleapis.com/css?family=Nunito" rel="stylesheet" type="text/css">

  <script src="https://js.stripe.com/v3/"></script>
  <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
  <!-- Styles -->
  <link href="{{ asset('css/app.css') }}" rel="stylesheet">
</head>

<body class="app header-fixed sidebar-fixed sidebar-lg-show">

  @yield('content')

  @yield('script')
</body>

</html>