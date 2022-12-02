<!DOCTYPE html>
<html lang="en" class="loading">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimal-ui">
    <meta name="description" content="">
    <meta name="keywords" content="Login">
    <meta name="author" content="Gravity Infotech">
    <title><?php echo e(Helper::webinfo()->site_title); ?> | <?php echo e(trans('labels.key_verification')); ?></title>
    <link rel="shortcut icon" type="image/png" href="<?php echo e(Helper::webinfo()->favicon); ?>">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-touch-fullscreen" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="default">
    <link href="https://fonts.googleapis.com/css?family=Rubik:300,400,500,700,900|Montserrat:300,400,500,600,700,800,900" rel="stylesheet">
    <!-- BEGIN VENDOR CSS-->
    <!-- font icons-->
    <link rel="stylesheet" type="text/css" href="<?php echo e(asset('storage/app/public/Adminassets/fonts/feather/style.min.css')); ?>">
    <link rel="stylesheet" type="text/css" href="<?php echo e(asset('storage/app/public/Adminassets/fonts/simple-line-icons/style.css')); ?>">
    <link rel="stylesheet" type="text/css" href="<?php echo e(asset('storage/app/public/Adminassets/fonts/font-awesome/css/font-awesome.min.css')); ?>">
    <link rel="stylesheet" type="text/css" href="<?php echo e(asset('storage/app/public/Adminassets/vendors/css/perfect-scrollbar.min.css')); ?>">
    <link rel="stylesheet" type="text/css" href="<?php echo e(asset('storage/app/public/Adminassets/vendors/css/prism.min.css')); ?>">
    <link rel="stylesheet" type="text/css" href="<?php echo e(asset('storage/app/public/Adminassets/css/app.css')); ?>">
    
  </head>
  <body data-col="1-column" class=" 1-column  blank-page blank-page">
    <?php if(Session::has('error')): ?>
        <div class="alert alert-danger" style="text-align: center; color: #fff !important; font-weight: bold;">
            <?php echo e(Session::get('error')); ?>

        </div>
    <?php endif; ?>
    <div class="wrapper">
      <div class="main-panel">
        <div class="main-content">
          <div class="content-wrapper">
                <section id="login">
                    <div class="container-fluid gradient-red-pink">
                        <div class="row full-height-vh">
                            <div class="col-12 d-flex align-items-center justify-content-center">
                                <div class="card bg-blue-grey bg-darken-3 text-center width-400">
                                    <div class="card-body">
                                        <div class="card-block">

                                        	<?php if(session()->has('danger')): ?>
                                        	    <div class="alert alert-danger" style="text-align: center;">
                                        	        <?php echo e(session()->get('danger')); ?>

                                        	    </div>
                                        	<?php endif; ?>

                                            <div class="form-group mt-4">
                                                <div class="col-md-12">
                                                    <img alt="element 06" class="mb-1" src="<?php echo e(Helper::webinfo()->image); ?>" width="190">
                                                </div>
                                            </div>

                                            <form method="POST" class="mt-5 mb-5 login-input" action="<?php echo e(route('admin.systemverification')); ?>">
                                                <?php echo csrf_field(); ?>

                                                <div class="form-group">
                                                    <input id="envato_username" type="text" class="form-control <?php $__errorArgs = ['envato_username'];
$__bag = $errors->getBag($__errorArgs[1] ?? 'default');
if ($__bag->has($__errorArgs[0])) :
if (isset($message)) { $__messageOriginal = $message; }
$message = $__bag->first($__errorArgs[0]); ?> is-invalid <?php unset($message);
if (isset($__messageOriginal)) { $message = $__messageOriginal; }
endif;
unset($__errorArgs, $__bag); ?>" name="envato_username" value="<?php echo e(old('envato_username')); ?>" required autocomplete="envato_username" autofocus placeholder="<?php echo e(trans('labels.envato_username')); ?>">

                                                    <?php $__errorArgs = ['envato_username'];
$__bag = $errors->getBag($__errorArgs[1] ?? 'default');
if ($__bag->has($__errorArgs[0])) :
if (isset($message)) { $__messageOriginal = $message; }
$message = $__bag->first($__errorArgs[0]); ?>
                                                        <span class="invalid-feedback" role="alert">
                                                            <strong><?php echo e($message); ?></strong>
                                                        </span>
                                                    <?php unset($message);
if (isset($__messageOriginal)) { $message = $__messageOriginal; }
endif;
unset($__errorArgs, $__bag); ?>
                                                </div>

                                                <div class="form-group">
                                                    <input id="email" type="email" class="form-control <?php $__errorArgs = ['email'];
$__bag = $errors->getBag($__errorArgs[1] ?? 'default');
if ($__bag->has($__errorArgs[0])) :
if (isset($message)) { $__messageOriginal = $message; }
$message = $__bag->first($__errorArgs[0]); ?> is-invalid <?php unset($message);
if (isset($__messageOriginal)) { $message = $__messageOriginal; }
endif;
unset($__errorArgs, $__bag); ?>" name="email" value="<?php echo e(old('email')); ?>" required autocomplete="email" autofocus placeholder="<?php echo e(trans('labels.email')); ?>">

                                                    <?php $__errorArgs = ['email'];
$__bag = $errors->getBag($__errorArgs[1] ?? 'default');
if ($__bag->has($__errorArgs[0])) :
if (isset($message)) { $__messageOriginal = $message; }
$message = $__bag->first($__errorArgs[0]); ?>
                                                        <span class="invalid-feedback" role="alert">
                                                            <strong><?php echo e($message); ?></strong>
                                                        </span>
                                                    <?php unset($message);
if (isset($__messageOriginal)) { $message = $__messageOriginal; }
endif;
unset($__errorArgs, $__bag); ?>
                                                </div>

                                                <div class="form-group">
                                                    <input id="purchase_key" type="text" class="form-control <?php $__errorArgs = ['purchase_key'];
$__bag = $errors->getBag($__errorArgs[1] ?? 'default');
if ($__bag->has($__errorArgs[0])) :
if (isset($message)) { $__messageOriginal = $message; }
$message = $__bag->first($__errorArgs[0]); ?> is-invalid <?php unset($message);
if (isset($__messageOriginal)) { $message = $__messageOriginal; }
endif;
unset($__errorArgs, $__bag); ?>" name="purchase_key" required autocomplete="current-purchase_key" placeholder="<?php echo e(trans('labels.purchase_key')); ?>">

                                                    <?php $__errorArgs = ['purchase_key'];
$__bag = $errors->getBag($__errorArgs[1] ?? 'default');
if ($__bag->has($__errorArgs[0])) :
if (isset($message)) { $__messageOriginal = $message; }
$message = $__bag->first($__errorArgs[0]); ?>
                                                        <span class="invalid-feedback" role="alert">
                                                            <strong><?php echo e($message); ?></strong>
                                                        </span>
                                                    <?php unset($message);
if (isset($__messageOriginal)) { $message = $__messageOriginal; }
endif;
unset($__errorArgs, $__bag); ?>
                                                </div>

                                                <?php
                                                $text = str_replace('verification', '', url()->current());
                                                ?>

                                                <div class="form-group">
                                                    <input id="domain" type="hidden" class="form-control <?php $__errorArgs = ['domain'];
$__bag = $errors->getBag($__errorArgs[1] ?? 'default');
if ($__bag->has($__errorArgs[0])) :
if (isset($message)) { $__messageOriginal = $message; }
$message = $__bag->first($__errorArgs[0]); ?> is-invalid <?php unset($message);
if (isset($__messageOriginal)) { $message = $__messageOriginal; }
endif;
unset($__errorArgs, $__bag); ?>" name="domain" required autocomplete="current-domain" value="<?php echo e($text); ?>"  readonly="">
                                                </div>

                                                <button type="submit" class="btn login-form__btn submit w-100">
                                                    <?php echo e(__('Submit')); ?>

                                                </button>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>
            <!--Login Page Ends-->
          </div>
        </div>
      </div>
    </div>

    <script src="<?php echo e(asset('storage/app/public/Adminassets/vendors/js/core/jquery-3.2.1.min.js')); ?>" type="text/javascript"></script>
    <script src="<?php echo e(asset('storage/app/public/Adminassets/vendors/js/core/popper.min.js')); ?>" type="text/javascript"></script>
    <script src="<?php echo e(asset('storage/app/public/Adminassets/vendors/js/core/bootstrap.min.js')); ?>" type="text/javascript"></script>
    <script src="<?php echo e(asset('storage/app/public/Adminassets/vendors/js/perfect-scrollbar.jquery.min.js')); ?>" type="text/javascript"></script>
    <script src="<?php echo e(asset('storage/app/public/Adminassets/vendors/js/prism.min.js')); ?>" type="text/javascript"></script>
    <script src="<?php echo e(asset('storage/app/public/Adminassets/vendors/js/jquery.matchHeight-min.js')); ?>" type="text/javascript"></script>
    <script src="<?php echo e(asset('storage/app/public/Adminassets/vendors/js/screenfull.min.js')); ?>" type="text/javascript"></script>
    <script src="<?php echo e(asset('storage/app/public/Adminassets/vendors/js/pace/pace.min.js')); ?>" type="text/javascript"></script>

    <script src="<?php echo e(asset('storage/app/public/Adminassets/js/app-sidebar.js')); ?>" type="text/javascript"></script>
    <script src="<?php echo e(asset('storage/app/public/Adminassets/js/notification-sidebar.js')); ?>" type="text/javascript"></script>
    <script src="<?php echo e(asset('storage/app/public/Adminassets/js/customizer.js')); ?>" type="text/javascript"></script>

  </body>

</html><?php /**PATH E:\xampp\htdocs\e-com\admin\resources\views/Admin/auth/verification.blade.php ENDPATH**/ ?>