<div class="sidebar">
    <nav class="sidebar-nav">
        <ul class="nav">
        @if (auth()->user()->is_super_admin_account)
            <li class="nav-item">
                <a class="nav-link" href="/sadmin_dashboard">
                    <i class="nav-icon fas fa-tachometer-alt"></i> Dashboard
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="/schools">
                    <i class="nav-icon fas fa-landmark"></i> Schools
                </a>
            </li>

            
            <li class="nav-item">
                <a class="nav-link" href="/plans">
                    <i class="nav-icon far fa-handshake"></i> Plans
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="/custom-plans">
                    <i class="nav-icon fas fa-drafting-compass"></i> Custom Plans
                </a>
            </li>
            {{-- <li class="nav-title">Settings</li> --}}
            <li class="nav-item">
                <a class="nav-link" href="/settings">
                    <i class="nav-icon fas fa-tools"></i> Settings
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/activation">
                    <i class="nav-icon fas fa-charging-station"></i> Activation
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/privacy-policy">
                    <i class="nav-icon fas fa-lock"></i> Privacy Policy
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/terms">
                    <i class="nav-icon fas fa-allergies"></i> Terms & Conditions
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="/ios-products">
                    <i class="nav-icon fab fa-apple"></i> iOS Products
                </a>
            </li>
        @else 
            <li class="nav-item">
                <a class="nav-link" href="/dashboard">
                    <i class="nav-icon fas fa-tachometer-alt"></i> Dashboard
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="/school">
                    <i class="nav-icon fas fa-landmark"></i> School
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/drivers">
                    <i class="nav-icon fas fa-user-tie"></i> Drivers
                </a>
            </li>

            <li class="nav-item">
                <a class="nav-link" href="/parents">
                    <i class="nav-icon fas fa-users"></i> Parents
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="/buses">
                    <i class="nav-icon fas fa-bus-alt"></i> Buses
                </a>
            </li>
        @endif               
        </ul>

    </nav>
    <sidebar></sidebar>
</div>
